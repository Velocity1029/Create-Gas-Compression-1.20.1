package com.velocity1029.create_gas_compression.blocks.engines;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;

import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.velocity1029.create_gas_compression.blocks.engines.variants.IEngineVariant;
import com.velocity1029.create_gas_compression.config.CreateGasCompressionConfig;
import com.velocity1029.create_gas_compression.registry.CGCBlocks;
import com.velocity1029.create_gas_compression.registry.CGCFluids;
import com.velocity1029.create_gas_compression.registry.CGCTags;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class EngineBlockEntity extends GeneratingKineticBlockEntity {
    public boolean needsPower = false;
    private final IEngineVariant variant;
    public static final int DEFAULT_SPEED = 16;
    public static final int MAX_SPEED = 256;
    public boolean powered = false;
    private float actualSpeed = 0;
    private float actualStress = 0;
    private long prvFluid = -100000;
    private int fluidSpam = 0;




    private final Couple<FluidTank> tanks;

    public FluidTank fuelTank;
    protected FluidTank exhaustTank;
    protected FluidTank airTank;

    protected ScrollValueBehaviour generatedSpeed;

    protected LazyOptional<CombinedTankWrapper> fluidCapability;

    public EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, IEngineVariant variant) {
        super(type, pos, state);
        this.variant = variant;
        this.fuelTank = createInventory(CGCTags.CGCFluidTags.NATURAL_GAS.tag, false);
        this.exhaustTank = createInventory((Fluid)CGCFluids.CARBON_DIOXIDE.getSource(), true);
        this.airTank = createInventory((Fluid)CGCFluids.AIR.getSource(), false);
        this.tanks = Couple.create(this.fuelTank, this.exhaustTank);
        this.fluidCapability = LazyOptional.of(() -> new CombinedTankWrapper(fuelTank, airTank, exhaustTank));
    }

    public static BlockEntityBuilder.BlockEntityFactory<EngineBlockEntity> create(IEngineVariant variant) {
        return (type, pos, state) -> new EngineBlockEntity(type, pos, state, variant);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        int max = MAX_SPEED;
        generatedSpeed = new KineticScrollValueBehaviour(CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"),
                this, new MotorValueBox());
        generatedSpeed.requiresWrench();
        generatedSpeed.between(-max, max);
        generatedSpeed.value = DEFAULT_SPEED;
        generatedSpeed.withCallback(i -> this.updateGeneratedRotation());
        behaviours.add(generatedSpeed);
    }

    @Override
    public void initialize() {
        powered = getLevel().hasNeighborSignal(this.getBlockPos());
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!CGCBlocks.NATURAL_GAS_ENGINE.has(getBlockState()))
            return 0;
        return convertToDirection(generatedSpeed.getValue(), getBlockState().getValue(EngineBlock.FACING));
    }

    static class MotorValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 12.5);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Direction facing = state.getValue(EngineBlock.FACING);
            return super.getLocalOffset(level, pos, state).add(Vec3.atLowerCornerOf(facing.getNormal())
                    .scale(-1 / 16f));
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            super.rotate(level, pos, state, ms);
            Direction facing = state.getValue(EngineBlock.FACING);
            if (facing.getAxis() == Axis.Y)
                return;
            if (getSide() != Direction.UP)
                return;
            TransformStack.of(ms)
                    .rotateZDegrees(-AngleHelper.horizontalAngle(facing) + 180);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(EngineBlock.FACING);
            if (facing.getAxis() != Axis.Y && direction == Direction.DOWN)
                return false;
            return direction.getAxis() != facing.getAxis();
        }

    }

    public void write(CompoundTag compound, boolean clientPacket) {
        compound.put("Fuel", this.fuelTank.writeToNBT(new CompoundTag()));
        compound.put("Air", this.airTank.writeToNBT(new CompoundTag()));
        compound.put("Exhaust", this.exhaustTank.writeToNBT(new CompoundTag()));
        super.write(compound, clientPacket);
    }

    protected void read(CompoundTag compound, boolean clientPacket) {
        this.fuelTank.readFromNBT(compound.getCompound("Fuel"));
        this.airTank.readFromNBT(compound.getCompound("Air"));
        this.exhaustTank.readFromNBT(compound.getCompound("Exhaust"));
        super.read(compound, clientPacket);
    }

    public void invalidate() {
        super.invalidate();
        this.fluidCapability.invalidate();
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        return cap == ForgeCapabilities.FLUID_HANDLER ? this.fluidCapability.cast() : super.getCapability(cap, side);
    }

    public void notifyUpdate() {
        super.notifyUpdate();
    }

    public Couple<FluidTank> getTanks() {
        return this.tanks;
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LazyOptional<IFluidHandler> handler = this.getCapability(ForgeCapabilities.FLUID_HANDLER);
        Optional<IFluidHandler> resolve = handler.resolve();
        if (!resolve.isPresent()) {
            return false;
        } else {
            IFluidHandler tank = (IFluidHandler)resolve.get();
            if (tank.getTanks() == 0) {
                return false;
            } else {
                LangBuilder mb = CreateLang.translate("generic.unit.millibuckets", new Object[0]);
                CreateLang.translate("goggles.natural_gas_engine.info", new Object[0]).style(ChatFormatting.GRAY).forGoggles(tooltip);
                boolean isEmpty = true;

                for(int i = 0; i < tank.getTanks(); ++i) {
                    FluidStack fluidStack = tank.getFluidInTank(i);
                    if (!fluidStack.isEmpty()) {
                        CreateLang.fluidName(fluidStack).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
                        CreateLang.builder().add(CreateLang.number((double)fluidStack.getAmount()).add(mb).style(ChatFormatting.DARK_GREEN)).text(ChatFormatting.GRAY, " / ").add(CreateLang.number((double)tank.getTankCapacity(i)).add(mb).style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);
                        isEmpty = false;
                    }
                }

                if (tank.getTanks() > 1) {
                    if (isEmpty) {
                        tooltip.remove(tooltip.size() - 1);
                    }

                    return true;
                } else if (!isEmpty) {
                    return true;
                } else {
                    CreateLang.translate("gui.goggles.fluid_container.capacity", new Object[0]).add(CreateLang.number((double)tank.getTankCapacity(0)).add(mb).style(ChatFormatting.DARK_GREEN)).style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
                    return true;
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;

        float stressMultiplier = 1;
        long extraEnergy = 0;

        Direction dir = this.getBlockState().getValue(DirectionalKineticBlock.FACING);
//        if (level.getBlockState(getBlockPos().relative(dir.getOpposite())).getOptionalValue(DirectionalKineticBlock.FACING).orElse(dir.getOpposite()) == dir
//                && level.getBlockEntity(getBlockPos().relative(dir.getOpposite()))
//                instanceof MotorExtensionBlockEntity extension) {
//            stressMultiplier = extension.getMultiplier();
//            extraEnergy = extension.getVariant().getExtraCapacity();
//        }

//        mut.setMaxCapacity(extraEnergy + variant.getMaxCapacity());
//        speedBehavior.betweenValidated((int) -variant.getSpeed(), (int) variant.getSpeed());

        if (!level.isClientSide()) {
            int needed = (int) Math.ceil((variant.getStress() * stressMultiplier
                    * CreateGasCompressionConfig.getCommon().engineSUMultiplier.get())
                    * CreateGasCompressionConfig.getCommon().suToMJ.get()
                    * variant.getEnergyDensity());
//            p = needsPower == powered ? energy.internalExtract(needed, false) : 0;
            int presentFluid = tanks.get(true).getFluidAmount();
            if (presentFluid > needed) {
                actualSpeed = generatedSpeed.value;
                actualStress =
                        (float) Math.ceil((variant.getStress() * stressMultiplier
                                * CreateGasCompressionConfig.getCommon().engineSUMultiplier.get())
                                * (presentFluid / (float)needed));
            } else {
                actualSpeed = 0;
                actualStress = 0;
            }
            if (((actualSpeed != speed) || (actualStress != stress))) {
                updateGeneratedRotation();
                speed = actualSpeed;
                stress = actualStress;
            } else if (presentFluid != prvFluid && fluidSpam > 10) {
                this.sendData();
                prvFluid = tanks.get(true).getFluidAmount();
                fluidSpam = 0;
            }
        }
        fluidSpam++;
    }


    protected void onFluidStackChanged(FluidStack newFluidStack) {
        this.sendData();
    }

    protected SmartFluidTank createInventory(final Fluid validFluid, final boolean extractionAllowed) {
        return new SmartFluidTank(1000, this::onFluidStackChanged) {
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().isSame(validFluid);
            }

            public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(resource, action);
            }

            public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(maxDrain, action);
            }
        };
    }

    protected SmartFluidTank createInventory(final TagKey<Fluid> validFluid, final boolean extractionAllowed) {
        return new SmartFluidTank(1000, this::onFluidStackChanged) {
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().is(validFluid);
            }

            public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(resource, action);
            }

            public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(maxDrain, action);
            }
        };
    }

}