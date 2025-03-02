package com.velocity1029.create_gas_compression.blocks.engines.natural_gas;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import com.drmangotea.tfmg.blocks.engines.diesel.engine_expansion.DieselEngineExpansionBlockEntity;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.drmangotea.tfmg.registry.TFMGSoundEvents;
import com.drmangotea.tfmg.registry.TFMGTags.TFMGFluidTags;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class NaturalGasEngineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public WeakReference<PoweredShaftBlockEntity> target = new WeakReference((Object)null);
    public FluidTank fuelTank;
    protected FluidTank exhaustTank;
    protected FluidTank airTank;
    private int consumptionTimer = 0;
    public float engineStrength = 0.0F;
    private Couple<FluidTank> tanks;
    public BlockPos expansionPos;
    public DieselEngineExpansionBlockEntity expansionBE;
    protected LazyOptional<CombinedTankWrapper> fluidCapability;
    float prevAngle = 0.0F;

    public DieselEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.fuelTank = this.createInventory(TFMGFluidTags.DIESEL.tag, false);
        this.exhaustTank = this.createInventory((Fluid)TFMGFluids.CARBON_DIOXIDE.getSource(), true);
        this.airTank = this.createInventory((Fluid)TFMGFluids.AIR.getSource(), false);
        this.tanks = Couple.create(this.fuelTank, this.exhaustTank);
        this.fluidCapability = LazyOptional.of(() -> {
            return new CombinedTankWrapper(new IFluidHandler[]{this.fuelTank, this.airTank, this.exhaustTank});
        });
    }

    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this));
    }

    private void onDirectionChanged() {
    }

    public void tick() {
        super.tick();
        if (this.m_58900_().m_61143_(DieselEngineBlock.FACE) == AttachFace.WALL) {
            this.expansionPos = this.m_58899_().m_121945_(((Direction)this.m_58900_().m_61143_(HorizontalDirectionalBlock.f_54117_)).m_122424_());
        }

        if (this.m_58900_().m_61143_(DieselEngineBlock.FACE) == AttachFace.CEILING) {
            this.expansionPos = this.m_58899_().m_7494_();
        }

        if (this.m_58900_().m_61143_(DieselEngineBlock.FACE) == AttachFace.FLOOR) {
            this.expansionPos = this.m_58899_().m_7495_();
        }

        if (this.expansionBE == null) {
            if (this.f_58857_.m_7702_(this.expansionPos) instanceof DieselEngineExpansionBlockEntity) {
                this.expansionBE = (DieselEngineExpansionBlockEntity)this.f_58857_.m_7702_(this.expansionPos);
            } else {
                this.expansionBE = null;
            }
        }

        if (this.expansionBE != null && !(this.expansionBE instanceof DieselEngineExpansionBlockEntity)) {
            this.expansionBE = null;
        }

        PoweredShaftBlockEntity shaft = this.getShaft();
        if (shaft == null) {
            if (!this.f_58857_.m_5776_()) {
                if (shaft != null) {
                    if (shaft.m_58899_().m_121996_(this.f_58858_).equals(shaft.enginePos)) {
                        if (shaft.engineEfficiency != 0.0F) {
                            Direction facing = DieselEngineBlock.getFacing(this.m_58900_());
                            if (this.f_58857_.m_46749_(this.f_58858_.m_121945_(facing.m_122424_()))) {
                                shaft.update(this.f_58858_, 0, 0.0F);
                            }

                        }
                    }
                }
            }
        } else {
            boolean verticalTarget = false;
            BlockState shaftState = shaft.m_58900_();
            Direction.Axis targetAxis = Axis.X;
            Block var7 = shaftState.m_60734_();
            if (var7 instanceof IRotate) {
                IRotate ir = (IRotate)var7;
                targetAxis = ir.getRotationAxis(shaftState);
            }

            verticalTarget = targetAxis == Axis.Y;
            BlockState blockState = this.m_58900_();
            if (TFMGBlocks.DIESEL_ENGINE.has(blockState)) {
                Direction facing = DieselEngineBlock.getFacing(blockState);
                if (facing.m_122434_() == Axis.Y) {
                    facing = (Direction)blockState.m_61143_(HorizontalDirectionalBlock.f_54117_);
                }

                if (!this.f_58857_.f_46443_ && this.getShaft() != null) {
                    this.engineProcess(targetAxis, verticalTarget);
                }

                if (this.f_58857_.f_46443_) {
                    this.makeSound(targetAxis, verticalTarget);
                }

                int conveyedSpeedLevel = this.engineStrength == 0.0F ? 1 : (verticalTarget ? 1 : (int)GeneratingKineticBlockEntity.convertToDirection(1.0F, facing) * 2);
                if (targetAxis == Axis.Z) {
                    conveyedSpeedLevel *= -1;
                }

                float shaftSpeed = shaft.getTheoreticalSpeed();
                if (shaft.hasSource() && shaftSpeed != 0.0F && conveyedSpeedLevel != 0 && shaftSpeed > 0.0F != conveyedSpeedLevel > 0) {
                    conveyedSpeedLevel *= -1;
                }

            }
        }
    }

    private void makeSound(Direction.Axis targetAxis, boolean verticalTarget) {
        Float targetAngle = this.getTargetAngle();
        PoweredShaftBlockEntity ste = (PoweredShaftBlockEntity)this.target.get();
        if (ste != null) {
            PoweredShaftBlockEntity shaft = this.getShaft();
            if (!((FluidTank)this.tanks.get(true)).isEmpty() && !((FluidTank)this.tanks.get(true)).isEmpty()) {
                if (this.expansionBE != null) {
                    if (this.airTank.isEmpty() && this.expansionBE.airTank.isEmpty()) {
                        this.engineStrength = 0.0F;
                        shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
                        return;
                    }
                } else if (this.airTank.isEmpty()) {
                    this.engineStrength = 0.0F;
                    shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
                    return;
                }

                if (((FluidTank)this.tanks.get(false)).getFluidAmount() + 5 > 1000) {
                    this.engineStrength = 0.0F;
                    shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
                } else if (targetAngle != null) {
                    float angle = AngleHelper.deg((double)targetAngle);
                    angle += angle < 0.0F ? -105.0F : 285.0F;
                    angle %= 360.0F;
                    if (shaft != null && shaft.getSpeed() != 0.0F) {
                        if (angle >= 0.0F && (!(this.prevAngle > 180.0F) || !(angle < 180.0F))) {
                            this.prevAngle = angle;
                        } else if (!(angle < 0.0F) || this.prevAngle < -180.0F && angle > -180.0F) {
                            TFMGSoundEvents.DIESEL_ENGINE.playAt(this.f_58857_, this.f_58858_, 0.4F, 1.0F, false);
                            this.prevAngle = angle;
                        } else {
                            this.prevAngle = angle;
                        }
                    }
                }
            } else {
                this.engineStrength = 0.0F;
                shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
            }
        }
    }

    public int getConveyedSpeedLevel(float strength, Direction.Axis targetAxis, boolean verticalTarget) {
        Direction facing = SteamEngineBlock.getFacing(this.m_58900_());
        PoweredShaftBlockEntity shaft = this.getShaft();
        int conveyedSpeedLevel = this.engineStrength == 0.0F ? 1 : (verticalTarget ? 1 : (int)GeneratingKineticBlockEntity.convertToDirection(1.0F, facing) * 2);
        if (targetAxis == Axis.Z) {
            conveyedSpeedLevel *= -1;
        }

        float shaftSpeed = shaft.getTheoreticalSpeed();
        if (shaft.hasSource() && shaftSpeed != 0.0F && conveyedSpeedLevel != 0 && shaftSpeed > 0.0F != conveyedSpeedLevel > 0) {
            conveyedSpeedLevel *= -1;
        }

        return conveyedSpeedLevel;
    }

    private void engineProcess(Direction.Axis targetAxis, boolean verticalTarget) {
        PoweredShaftBlockEntity shaft = this.getShaft();
        if (!((FluidTank)this.tanks.get(true)).isEmpty() && !((FluidTank)this.tanks.get(true)).isEmpty()) {
            if (this.expansionBE != null) {
                if (this.airTank.isEmpty() && this.expansionBE.airTank.isEmpty()) {
                    this.engineStrength = 0.0F;
                    shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
                    return;
                }
            } else if (this.airTank.isEmpty()) {
                this.engineStrength = 0.0F;
                shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
                return;
            }

            if (((FluidTank)this.tanks.get(false)).getFluidAmount() + 5 > 1000) {
                this.engineStrength = 0.0F;
                shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
            } else {
                int strengthModifier;
                if (this.consumptionTimer >= 5) {
                    strengthModifier = 0;
                    if (this.expansionBE != null && !this.expansionBE.coolantTank.isEmpty()) {
                        strengthModifier = 1;
                    }

                    this.fuelTank.setFluid(new FluidStack(TFMGFluids.DIESEL.getSource(), this.fuelTank.getFluidAmount() - (2 - strengthModifier)));
                }

                if (!this.airTank.isEmpty()) {
                    this.airTank.setFluid(new FluidStack(TFMGFluids.AIR.getSource(), this.airTank.getFluidAmount() - 5));
                } else {
                    this.expansionBE.airTank.setFluid(new FluidStack(TFMGFluids.AIR.getSource(), this.expansionBE.airTank.getFluidAmount() - 5));
                }

                this.exhaustTank.fill(new FluidStack(TFMGFluids.CARBON_DIOXIDE.getSource(), 3), FluidAction.EXECUTE);
                strengthModifier = 0;
                if (this.expansionBE != null) {
                    if (!this.expansionBE.coolantTank.isEmpty()) {
                        strengthModifier += 3;
                        if (this.consumptionTimer > 5) {
                            this.expansionBE.coolantTank.setFluid(new FluidStack(TFMGFluids.COOLING_FLUID.getSource(), this.expansionBE.coolantTank.getFluidAmount() - 1));
                        }
                    }

                    if (!this.expansionBE.lubricationOilTank.isEmpty()) {
                        strengthModifier += 5;
                        if (this.consumptionTimer > 5) {
                            this.expansionBE.lubricationOilTank.setFluid(new FluidStack(TFMGFluids.LUBRICATION_OIL.getSource(), this.expansionBE.lubricationOilTank.getFluidAmount() - 1));
                        }
                    }
                }

                if (!this.f_58857_.m_8055_(this.expansionPos).m_60713_((Block)TFMGBlocks.DIESEL_ENGINE_EXPANSION.get())) {
                    strengthModifier = 0;
                }

                if (this.consumptionTimer > 5) {
                    this.consumptionTimer = 0;
                }

                ++this.consumptionTimer;
                this.engineStrength = (float)(40 + strengthModifier);
                shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
                this.sendData();
                this.m_6596_();
            }
        } else {
            this.engineStrength = 0.0F;
            shaft.update(this.f_58858_, this.getConveyedSpeedLevel(this.engineStrength, targetAxis, verticalTarget), this.engineStrength);
        }
    }

    public void remove() {
        PoweredShaftBlockEntity shaft = this.getShaft();
        if (shaft != null) {
            shaft.remove(this.f_58858_);
        }

        super.remove();
    }

    @OnlyIn(Dist.CLIENT)
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().m_82400_(2.0);
    }

    public PoweredShaftBlockEntity getShaft() {
        PoweredShaftBlockEntity shaft = (PoweredShaftBlockEntity)this.target.get();
        if (shaft == null || shaft.m_58901_() || !shaft.canBePoweredBy(this.f_58858_)) {
            if (shaft != null) {
                this.target = new WeakReference((Object)null);
            }

            Direction facing = DieselEngineBlock.getFacing(this.m_58900_());
            BlockEntity anyShaftAt = this.f_58857_.m_7702_(this.f_58858_.m_5484_(facing, 2));
            if (anyShaftAt instanceof PoweredShaftBlockEntity) {
                PoweredShaftBlockEntity ps = (PoweredShaftBlockEntity)anyShaftAt;
                if (ps.canBePoweredBy(this.f_58858_)) {
                    shaft = ps;
                    this.target = new WeakReference(ps);
                }
            }
        }

        return shaft;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Float getTargetAngle() {
        float angle = 0.0F;
        BlockState blockState = this.m_58900_();
        if (!TFMGBlocks.DIESEL_ENGINE.has(blockState)) {
            return null;
        } else {
            Direction facing = SteamEngineBlock.getFacing(blockState);
            PoweredShaftBlockEntity shaft = this.getShaft();
            Direction.Axis facingAxis = facing.m_122434_();
            Direction.Axis axis = Axis.Y;
            if (shaft == null) {
                return null;
            } else {
                axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
                angle = KineticBlockEntityRenderer.getAngleForTe(shaft, shaft.m_58899_(), axis);
                if (axis == facingAxis) {
                    return null;
                } else {
                    if (axis.m_122479_() && facingAxis == Axis.X ^ facing.m_122421_() == AxisDirection.POSITIVE) {
                        angle *= -1.0F;
                    }

                    if (axis == Axis.X && facing == Direction.DOWN) {
                        angle *= -1.0F;
                    }

                    return angle;
                }
            }
        }
    }

    public void write(CompoundTag compound, boolean clientPacket) {
        compound.m_128365_("Fuel", this.fuelTank.writeToNBT(new CompoundTag()));
        compound.m_128365_("Air", this.airTank.writeToNBT(new CompoundTag()));
        compound.m_128365_("Exhaust", this.exhaustTank.writeToNBT(new CompoundTag()));
        super.write(compound, clientPacket);
    }

    protected void read(CompoundTag compound, boolean clientPacket) {
        this.fuelTank.readFromNBT(compound.m_128469_("Fuel"));
        this.airTank.readFromNBT(compound.m_128469_("Air"));
        this.exhaustTank.readFromNBT(compound.m_128469_("Exhaust"));
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
                LangBuilder mb = Lang.translate("generic.unit.millibuckets", new Object[0]);
                Lang.translate("goggles.diesel_engine.info", new Object[0]).style(ChatFormatting.GRAY).forGoggles(tooltip);
                boolean isEmpty = true;

                for(int i = 0; i < tank.getTanks(); ++i) {
                    FluidStack fluidStack = tank.getFluidInTank(i);
                    if (!fluidStack.isEmpty()) {
                        Lang.fluidName(fluidStack).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
                        Lang.builder().add(Lang.number((double)fluidStack.getAmount()).add(mb).style(ChatFormatting.DARK_GREEN)).text(ChatFormatting.GRAY, " / ").add(Lang.number((double)tank.getTankCapacity(i)).add(mb).style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);
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
                    Lang.translate("gui.goggles.fluid_container.capacity", new Object[0]).add(Lang.number((double)tank.getTankCapacity(0)).add(mb).style(ChatFormatting.DARK_GREEN)).style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
                    return true;
                }
            }
        }
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        this.sendData();
    }

    protected SmartFluidTank createInventory(final Fluid validFluid, final boolean extractionAllowed) {
        return new SmartFluidTank(1000, this::onFluidStackChanged) {
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().m_6212_(validFluid);
            }

            public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(resource, action);
            }

            public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(maxDrain, action);
            }
        };
    }

    protected SmartFluidTank createInventory(final TagKey<Fluid> validFluid, final boolean extractionAllowed) {
        return new SmartFluidTank(1000, this::onFluidStackChanged) {
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().m_205067_(validFluid);
            }

            public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(resource, action);
            }

            public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
                return !extractionAllowed ? FluidStack.EMPTY : super.drain(maxDrain, action);
            }
        };
    }
}
