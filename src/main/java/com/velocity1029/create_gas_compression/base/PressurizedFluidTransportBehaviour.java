package com.velocity1029.create_gas_compression.base;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidReactions;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.blocks.pipes.GlassIronPipeBlock;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlock;
import com.velocity1029.create_gas_compression.registry.CGCTags;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.function.Predicate;

public class PressurizedFluidTransportBehaviour extends FluidTransportBehaviour {

    public static final BehaviourType<PressurizedFluidTransportBehaviour> TYPE = new BehaviourType<>();

    public PressurizedFluidTransportBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public boolean canHaveFlowToward(BlockState state, Direction direction) {
        return (IronPipeBlock.isPipe(state) || state.getBlock() instanceof EncasedPipeBlock)
                && state.getValue(IronPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
    }

    @Override
    public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                    Direction direction) {
        AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);

        BlockPos offsetPos = pos.relative(direction);
        BlockState otherState = world.getBlockState(offsetPos);

        if (state.getBlock() instanceof EncasedPipeBlock && attachment != AttachmentTypes.DRAIN)
            return AttachmentTypes.NONE;

        if (attachment == AttachmentTypes.RIM) {
            if (!IronPipeBlock.isPipe(otherState) && !(otherState.getBlock() instanceof EncasedPipeBlock)
                    && !(otherState.getBlock() instanceof GlassIronPipeBlock)) {
                PressurizedFluidTransportBehaviour pipeBehaviour =
                        BlockEntityBehaviour.get(world, offsetPos, PressurizedFluidTransportBehaviour.TYPE);
                if (pipeBehaviour != null && pipeBehaviour.canHaveFlowToward(otherState, direction.getOpposite()))
                    return AttachmentTypes.DETAILED_CONNECTION;
            }

            if (!IronPipeBlock.shouldDrawRim(world, pos, state, direction))
                return FluidPropagator.getStraightPipeAxis(state) == direction.getAxis()
                        ? AttachmentTypes.CONNECTION
                        : AttachmentTypes.DETAILED_CONNECTION;
        }

        if (attachment == AttachmentTypes.NONE
                && state.getValue(IronPipeBlock.PROPERTY_BY_DIRECTION.get(direction)))
            return AttachmentTypes.DETAILED_CONNECTION;

        return attachment;
    }

	public void visualizePressure(PipeConnection connection, BlockPos pos) {
		if (!connection.hasPressure())
			return;

        connection.getPressure().forEachWithContext((pressure, inbound) -> {
			if (inbound)
				return;

			Vec3 source = Vec3.atCenterOf(connection.side.getNormal());
            Vec3 target = pos.getCenter();
			Vec3 scaleVec = source.scale(-.25f * connection.side.getAxisDirection().getStep());
            source = source.scale(inbound ? .35f : .45f);
            target = source.add(scaleVec.scale(1/64f));
			Outliner.getInstance().chaseAABB("pressure" + pos.toShortString() + connection.side.getName() + String.valueOf(inbound),
                new AABB(Vec3.atLowerCornerOf(pos), Vec3.atLowerCornerOf(pos).add(1,1,1))
//                        .move(source.add(VecHelper.getCenterOf(pos)))
                        .inflate(scaleVec.x, scaleVec.y, scaleVec.z)
                        .expandTowards(0, pressure / 64f, 0)
                        .inflate(1 / 64f)).colored(inbound ? Color.GREEN : Color.RED);
//				directionVec.add(new Vector3d(pos.getX(), pos.getY(), pos.getZ()))
//					.grow(scaleVec.x, scaleVec.y, scaleVec.z)
//					.expand(0, pressure / 64f, 0)
//					.grow(1 / 64f));
		});
	}

//	void visualizeFlow(PipeConnection connection, BlockPos pos) {
//		if (!connection.hasFlow())
//			return;
//
//        Vec3 directionVec = new Vec3(connection.side.step());
//		float size = 1 / 4f;
//		float length = .5f;
//		PipeConnection.Flow flow = connection.flow.get();
//		boolean inbound = flow.inbound;
//		FluidStack fluid = flow.fluid;
//
//		if (flow.progress == null)
//			return;
//		float value = flow.progress.getValue();
//        Vec3 start = directionVec.scale(inbound ? .5 : .5f - length);
//        Vec3 offset = directionVec.scale(length * (inbound ? -1 : 1))
//			.scale(value);
//
//		Vec3 scale = new Vec3(1, 1, 1).subtract(directionVec.scale(connection.side.getAxisDirection()
//            .getStep()))
//			.scale(size);
//		AABB bb = new AABB(start, start.add(offset)).move(VecHelper.getCenterOf(pos))
//                .inflate(scale.x, scale.y, scale.z);
//
//		int color = 0x7fdbda;
//		if (!fluid.isEmpty()) {
//			Fluid fluid2 = fluid.getFluid();
//			if (fluid2 == Fluids.WATER)
//				color = 0x1D4D9B;
//			else if (fluid2 == Fluids.LAVA)
//				color = 0xFF773D;
//			else
//				color = fluid2.getAttributes()
//					.getColor(fluid);
//		}
//
//		Outliner.getInstance().chaseAABB(this, bb)
//			.withFaceTexture(AllSpecialTextures.SELECTION)
//			.colored(color)
//			.lineWidth(0);
//	}

    @Override
    public void tick() {
        super.tick();
        Level world = getWorld();
        BlockPos pos = getPos();
        boolean onServer = !world.isClientSide || blockEntity.isVirtual();

        if (interfaces == null)
            return;
        Collection<PipeConnection> connections = interfaces.values();

        // Do not provide a lone pipe connection with its own flow input
        PipeConnection singleSource = null;

		if (world.isClientSide) {
			connections.forEach(connection -> {
                visualizePressure(connection, pos);
//				connection.visualizeFlow(pos);
//				connection.visualizePressure(pos);
			});
		}

        if (phase == UpdatePhase.WAIT_FOR_PUMPS) {
            phase = UpdatePhase.FLIP_FLOWS;
            return;
        }

        if (onServer) {
            boolean sendUpdate = false;
            for (PipeConnection connection : connections) {
                sendUpdate |= connection.flipFlowsIfPressureReversed();
                connection.manageSource(world, pos);
            }
            if (sendUpdate)
                blockEntity.notifyUpdate();
        }

        if (phase == UpdatePhase.FLIP_FLOWS) {
            phase = UpdatePhase.IDLE;
            return;
        }

        if (onServer) {
            FluidStack availableFlow = FluidStack.EMPTY;
            FluidStack collidingFlow = FluidStack.EMPTY;

            for (PipeConnection connection : connections) {
                FluidStack fluidInFlow = connection.getProvidedFluid();
                if (fluidInFlow.isEmpty())
                    continue;
                if (availableFlow.isEmpty()) {
                    singleSource = connection;
                    availableFlow = fluidInFlow;
                    continue;
                }
                if (availableFlow.isFluidEqual(fluidInFlow)) {
                    singleSource = null;
                    availableFlow = fluidInFlow;
                    continue;
                }
                collidingFlow = fluidInFlow;
                break;
            }

            if (!collidingFlow.isEmpty()) {
                FluidReactions.handlePipeFlowCollision(world, pos, availableFlow, collidingFlow);
                return;
            }

            boolean sendUpdate = false;
            for (PipeConnection connection : connections) {
                FluidStack internalFluid = singleSource != connection ? availableFlow : FluidStack.EMPTY;

                // Pressure explosion functionality
                CompoundTag fluidTags = internalFluid.getTag();
                if (fluidTags != null && fluidTags.contains("Pressure", Tag.TAG_FLOAT) && fluidTags.getInt("Pressure") > 0) {
                    // If the connected block isn't pressurized
                    BlockPos connectedPos = pos.relative(connection.side);
                    BlockState connectedBlock = world.getBlockState(connectedPos);
                    if (!connectedBlock.is(CGCTags.CGCBlockTags.PRESSURIZED.tag)) {
                        // Burst!
                        float strength = fluidTags.getInt("Pressure");
                        world.explode(null, connectedPos.getX(), connectedPos.getY(), connectedPos.getZ(), strength, Level.ExplosionInteraction.BLOCK);
                    }

                }
                Predicate<FluidStack> extractionPredicate =
                        extracted -> canPullFluidFrom(extracted, blockEntity.getBlockState(), connection.side);
                sendUpdate |= connection.manageFlows(world, pos, internalFluid, extractionPredicate);
            }

            if (sendUpdate)
                blockEntity.notifyUpdate();
        }

        for (PipeConnection connection : connections)
            connection.tickFlowProgress(world, pos);
    }
}
