package com.dori.SpringStory.connection.packet.packets;

import com.dori.SpringStory.client.character.ExtendSP;
import com.dori.SpringStory.client.character.Skill;
import com.dori.SpringStory.client.messages.IncEXPMessage;
import com.dori.SpringStory.connection.packet.OutPacket;
import com.dori.SpringStory.connection.packet.handlers.StageHandler;
import com.dori.SpringStory.connection.packet.headers.OutHeader;
import com.dori.SpringStory.enums.InventoryOperation;
import com.dori.SpringStory.enums.InventoryType;
import com.dori.SpringStory.enums.Stat;
import com.dori.SpringStory.inventory.Equip;
import com.dori.SpringStory.inventory.Item;
import com.dori.SpringStory.logger.Logger;

import java.util.*;

import static com.dori.SpringStory.enums.InventoryType.EQUIPPED;
import static com.dori.SpringStory.enums.MessageType.*;

public interface CWvsContext {
    // Logger -
    Logger logger = new Logger(CWvsContext.class);

    static OutPacket inventoryOperation(boolean exclRequestSent, InventoryOperation type, short oldPos, short newPos,
                                        Item item) {
        InventoryType invType = item.getInvType();
        if ((oldPos > 0 && newPos < 0 && invType == EQUIPPED) || (invType == EQUIPPED && oldPos < 0)) {
            invType = InventoryType.EQUIP;
        }
        OutPacket outPacket = new OutPacket(OutHeader.InventoryOperation);
        outPacket.encodeBool(exclRequestSent);
        outPacket.encodeByte(1); // size
        // For each operation - (tho I see it always 1 at a time...)
        outPacket.encodeByte(type.getVal());
        outPacket.encodeByte(invType.getVal());
        outPacket.encodeShort(oldPos);
        // Handling for the diff operations -
        switch (type) {
            case Add -> item.encode(outPacket);
            case UpdateQuantity -> outPacket.encodeShort(item.getQuantity());
            case Move -> outPacket.encodeShort(newPos);
            case Remove -> {/*Do nothing O.o*/}
            case ItemExp -> outPacket.encodeLong(((Equip) item).getExp());
        }
        // Related to the case if you drop an equip straight to the field -
        outPacket.encodeBool(!(oldPos >= 0)); // bSN == bStat
        return outPacket;
    }

    static OutPacket statChanged(Map<Stat, Object> stats, boolean exclRequestSent, byte charm,
                                 int hpRecovery, int mpRecovery) {
        OutPacket outPacket = new OutPacket(OutHeader.StatChanged);

        outPacket.encodeByte(exclRequestSent); // enableActions
        // GW_CharacterStat::DecodeChangeStat
        int mask = 0;
        for (Stat stat : stats.keySet()) {
            mask |= stat.getVal();
        }
        outPacket.encodeInt(mask);
        // Sort the Stats by their mask val -
        List<Map.Entry<Stat, Object>> sortedListOfStats = new ArrayList<>(stats.entrySet());
        sortedListOfStats.sort(Comparator.comparingInt(stat -> stat.getKey().getVal()));
        // Encode Stats -
        sortedListOfStats.forEach(stat -> {
            try {
                int statValue = 0;
                if (stat.getKey() != Stat.SkillPoint) {
                    if (stat.getValue() instanceof Integer) {
                        statValue = (Integer) stat.getValue();
                    } else if (stat.getValue() instanceof Short) {
                        statValue = ((Short) stat.getValue()).intValue();
                    } else if (stat.getValue() instanceof Long) {
                        statValue = ((Long) stat.getValue()).intValue();
                    }
                }
                switch (stat.getKey()) {
                    case Skin, Level -> outPacket.encodeByte((byte) statValue);
                    case Face, Hair, Hp, MaxHp, Mp, MaxMp, Exp, Money -> outPacket.encodeInt(statValue);
                    case SubJob, Str, Dex, Inte, Luk, AbilityPoint, Pop -> outPacket.encodeShort((short) statValue);
                    case SkillPoint -> {
                        if (stat.getValue() instanceof ExtendSP) {
                            ((ExtendSP) stat.getValue()).encode(outPacket);
                        } else {
                            outPacket.encodeShort(((Integer) stat.getValue()).shortValue());
                        }
                    }
                    case Pet, Pet2, Pet3 -> outPacket.encodeLong((long) stat.getValue());
                    case TempExp -> logger.warning("Attempt to change TempExp, which isn't implemented!");
                }
            } catch (Exception e) {
                logger.error("error occurred!");
                e.printStackTrace();
            }
        });
        // Encode Charm -
        boolean isCharm = charm > 0;
        outPacket.encodeBool(isCharm);
        if (isCharm) {
            outPacket.encodeByte(charm);
        }
        // Encode Recovery -
        boolean isRecovery = hpRecovery > 0 && mpRecovery > 0;
        outPacket.encodeBool(isRecovery);
        if (isRecovery) {
            outPacket.encodeInt(hpRecovery);
            outPacket.encodeInt(mpRecovery);
        }
        return outPacket;
    }

    static OutPacket dropPickupMessage(int mesoAmountOrItemID, byte type, short internetCafeExtra, short quantity) {
        OutPacket outPacket = new OutPacket(OutHeader.Message);

        outPacket.encodeByte(DROP_PICK_UP_MESSAGE.getVal());
        outPacket.encodeByte(type);
        switch (type) {
            case 0 -> {
                // Item pickup message -
                outPacket.encodeInt(mesoAmountOrItemID);
                outPacket.encodeInt(quantity);
            }
            case 1 -> {
                // Meso pickup message -
                outPacket.encodeBool(false); // Portion was lost after falling to the ground
                outPacket.encodeInt(mesoAmountOrItemID); // Meso amount
                outPacket.encodeShort(internetCafeExtra); // Internet cafe
            }
            case 2 -> {
                // IDK?
                outPacket.encodeInt(100);
            }
        }

        return outPacket;
    }

    static OutPacket cashItemExpireMessage(int itemID) {
        OutPacket outPacket = new OutPacket(OutHeader.Message);

        outPacket.encodeByte(CASH_ITEM_EXPIRE_MESSAGE.getVal());
        outPacket.encodeInt(itemID);

        return outPacket;
    }

    static OutPacket incExpMessage(IncEXPMessage incEXPMessage) {
        OutPacket outPacket = new OutPacket(OutHeader.Message);

        outPacket.encodeByte(INC_EXP_MESSAGE.getVal());
        incEXPMessage.encode(outPacket);

        return outPacket;
    }

    static OutPacket incSpMessage(short job, byte amount) {
        OutPacket outPacket = new OutPacket(OutHeader.Message);

        outPacket.encodeByte(INC_SP_MESSAGE.getVal());
        outPacket.encodeShort(job);
        outPacket.encodeByte(amount);

        return outPacket;
    }

    static OutPacket incMoneyMessage(int amount) {
        OutPacket outPacket = new OutPacket(OutHeader.Message);

        outPacket.encodeByte(INC_MESO_MESSAGE.getVal());
        outPacket.encodeInt(amount);

        return outPacket;
    }

    static OutPacket incPopMessage(int amount) {
        OutPacket outPacket = new OutPacket(OutHeader.Message);

        outPacket.encodeByte(INC_FAME_MESSAGE.getVal());
        outPacket.encodeInt(amount);

        return outPacket;
    }

    static OutPacket changeSkillRecordResult(Set<Skill> skills, boolean exclRequestSent, boolean bSN) {
        OutPacket outPacket = new OutPacket(OutHeader.ChangeSkillRecordResult);

        outPacket.encodeBool(exclRequestSent);
        outPacket.encodeShort(skills.size());
        skills.forEach(skill -> skill.encode(outPacket));
        outPacket.encodeBool(bSN);

        return outPacket;
    }
}
