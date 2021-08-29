package eu.gir.girsignals.linkableApi;

import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Linkingtool extends Item {

	final BiPredicate<World, BlockPos> predicate;

	public Linkingtool(final CreativeTabs tab, final BiPredicate<World, BlockPos> predicate) {
		setCreativeTab(tab);
		this.predicate = predicate;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return EnumActionResult.PASS;
		final TileEntity entity = worldIn.getTileEntity(pos);
		final ItemStack stack = player.getHeldItem(hand);
		if (entity instanceof ILinkableTile) {
			ILinkableTile controller = ((ILinkableTile) worldIn.getTileEntity(pos));
			if (!player.isSneaking()) {
				final NBTTagCompound comp = stack.getTagCompound();
				if (comp == null) {
					player.sendMessage(new TextComponentTranslation("lt.notset", pos.toString()));
					return EnumActionResult.PASS;
				}
				final BlockPos lpos = NBTUtil.getPosFromTag(comp);
				if (controller.link(lpos)) {
					player.sendMessage(new TextComponentTranslation("lt.linkedpos"));
					stack.setTagCompound(null);
					player.sendMessage(new TextComponentTranslation("lt.reset"));
					return EnumActionResult.FAIL;
				}
				player.sendMessage(new TextComponentTranslation("lt.notlinked"));
				player.sendMessage(new TextComponentTranslation("lt.notlinked.msg"));
				return EnumActionResult.FAIL;
			} else {
				if (controller.hasLink() && controller.unlink()) {
					player.sendMessage(new TextComponentTranslation("lt.unlink"));
				}
			}
			return EnumActionResult.SUCCESS;
		} else if (predicate.test(worldIn, pos)) {
			if(stack.getTagCompound() != null) {
				
				return EnumActionResult.FAIL;
			}
			final NBTTagCompound comp = NBTUtil.createPosTag(pos);
			stack.setTagCompound(comp);
			player.sendMessage(new TextComponentTranslation("lt.setpos", pos.getX(), pos.getY(), pos.getZ()));
			player.sendMessage(new TextComponentTranslation("lt.setpos.msg"));
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("lt.notlinked"));
		tooltip.add(I18n.format("lt.notlinked.msg"));
	}
}