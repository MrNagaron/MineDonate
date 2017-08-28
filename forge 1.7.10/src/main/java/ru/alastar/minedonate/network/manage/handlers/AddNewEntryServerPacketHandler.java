package ru.alastar.minedonate.network.manage.handlers;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.alastar.minedonate.MineDonate;
import ru.alastar.minedonate.RunnableTask;
import ru.alastar.minedonate.network.manage.packets.AddNewEntryPacket;
import ru.alastar.minedonate.network.manage.packets.ManageResponsePacket;
import ru.alastar.minedonate.rtnl.Account;
import ru.alastar.minedonate.rtnl.Manager;
import ru.alastar.minedonate.rtnl.ModNetwork;
import ru.alastar.minedonate.rtnl.Shop;

import java.sql.Connection;

public class AddNewEntryServerPacketHandler implements IMessageHandler < AddNewEntryPacket, IMessage > {
	
    public AddNewEntryServerPacketHandler ( ) {

    }

    @Override
    public IMessage onMessage (final AddNewEntryPacket message, final MessageContext ctx ) {

    	MineDonate.db_worker.AddTask(new RunnableTask() {
			@Override
			public void run(Connection conn) {

				EntityPlayerMP serverPlayer = ctx . getServerHandler ( ) . playerEntity ;
				System.out.println("Add new entry");
				if ( ! MineDonate . checkShopExists ( message . shopId ) ) {
					ModNetwork.sendTo(serverPlayer, new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_SHOP_NOTFOUND ));

				}


				Shop s = MineDonate . shops . get ( message . shopId ) ;

				Account acc = MineDonate . getAccount ( serverPlayer . getDisplayName ( ) . toLowerCase ( ) ) ;

				if ( acc . canEditShop ( s . owner ) ) {

					if ( s . isFreezed ) {

						ModNetwork.sendTo(serverPlayer, new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_SHOP_FREEZED ));

					}

					if ( message . name == null || message . name . length ( ) > 140 || message . cost < 1 ) {

						ModNetwork.sendTo(serverPlayer, new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_UNKNOWN )) ;

					}

					if ( ! MineDonate . checkCatExists ( s . sid, message . catId ) ) {

						ModNetwork.sendTo(serverPlayer, new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_CAT_NOTFOUND )) ;

					}

					switch ( s . cats [ message . catId ] . getCatType ( ) ) {

						case ITEMS :

							if ( acc . ms . currentItemStack == null ) {

								ModNetwork.sendTo(serverPlayer, new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_UNKNOWN )) ;

							}

							if ( message . limit < 0 && ! acc . canUnlimitedItems ( ) ) {

								ModNetwork.sendTo(serverPlayer, new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_ACCESS_DENIED ) ) ;

							}

							Manager . addItemToShop ( acc, s, message . catId, message . limit, message . cost, message . name, conn ) ;

							break ;

						case ENTITIES :

							if ( acc . ms . currentMob == null ) {

								ModNetwork.sendTo(serverPlayer,  new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_UNKNOWN ) ) ;

							}

							if ( message . limit < 0 && ! acc . canUnlimitedEntities ( ) ) {

								ModNetwork.sendTo(serverPlayer,  new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_ACCESS_DENIED ) ) ;

							}

							Manager . addEntityToShop ( acc, s, message . catId, message . limit, message . cost, message . name, conn ) ;

							break ;

					}

					ModNetwork.sendTo(serverPlayer,  new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.OK ) );

				} else {

					ModNetwork.sendTo(serverPlayer, new ManageResponsePacket ( ManageResponsePacket.ResponseType.OBJ, ManageResponsePacket.ResponseCode.ADD, ManageResponsePacket.ResponseStatus.ERROR_ACCESS_DENIED ) );

				}
			}
		});
		return null;
    }
    
}