package com.dori.Dori90v.utils;

import com.dori.Dori90v.client.character.MapleChar;
import com.dori.Dori90v.enums.BodyPart;
import com.dori.Dori90v.enums.EquipPrefix;
import com.dori.Dori90v.inventory.Equip;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface ItemUtils {

    private static int getItemPrefix(int nItemID) {
        return nItemID / 10000;
    }

    private static int getGenderFromItemID(int nItemID) {
        if (nItemID / 1000000 != 1 && getItemPrefix(nItemID) != 254 || getItemPrefix(nItemID) == 119 || getItemPrefix(nItemID) == 168) {
            return 2;
        }
        int result;
        switch (nItemID / 1000 % 10) {
            case 0 -> result = 0;
            case 1 -> result = 1;
            default -> result = 2;
        }
        return result;
    }

    static BodyPart getBodyPartFromItem(int itemID) {
        EquipPrefix prefix = EquipPrefix.getByVal(getItemPrefix(itemID));
        BodyPart bodyPart = BodyPart.BPBase;
        if (prefix != null) {
            switch (prefix) {
                case Hat -> bodyPart = BodyPart.Hat;
                case FaceAccessory -> bodyPart = BodyPart.FaceAccessory;
                case EyeAccessory -> bodyPart = BodyPart.EyeAccessory;
                case Earrings -> bodyPart = BodyPart.Earrings;
                case Top, Overall -> bodyPart = BodyPart.Top;
                case Bottom -> bodyPart = BodyPart.Bottom;
                case Shoes -> bodyPart = BodyPart.Shoes;
                case Gloves -> bodyPart = BodyPart.Gloves;
                case Shield, Katana, SecondaryWeapon -> bodyPart = BodyPart.Shield;
                case Cape -> bodyPart = BodyPart.Cape;
                case Ring -> bodyPart = BodyPart.Ring1;
                case Pendant -> bodyPart = BodyPart.Pendant;
                case Belt -> bodyPart = BodyPart.Belt;
                case Medal -> bodyPart = BodyPart.Medal;
                case Shoulder -> bodyPart = BodyPart.Shoulder;
                case MonsterBook -> bodyPart = BodyPart.MonsterBook;
                case PetWear -> bodyPart = BodyPart.PetWear1;
                case TamingMob -> bodyPart = BodyPart.TamingMob;
                case Saddle -> bodyPart = BodyPart.Saddle;
                case EvanHat -> bodyPart = BodyPart.EvanHat;
                case EvanPendant -> bodyPart = BodyPart.EvanPendant;
                case EvanWing -> bodyPart = BodyPart.EvanWing;
                case EvanShoes -> bodyPart = BodyPart.EvanShoes;
                case OneHandedAxe,OneHandedSword,OneHandedBluntWeapon,TwoHandedBluntWeapon,TwoHandedAxe,TwoHandedSword -> bodyPart =BodyPart.Weapon;
                default -> System.out.println("idk? " + prefix);
            }
        }
        return bodyPart;
    }

    static void fillEquipsMaps(MapleChar chr,
                               Map<BodyPart, Integer> charEquips,
                               Map<BodyPart, Integer> charMaskedEquips,
                               List<Integer> cWeapon) {
        for (Equip item : chr.getEquippedInventory().getItems()) {
            BodyPart bodyPart = getBodyPartFromItem(item.getItemId());
            if(bodyPart != BodyPart.BPBase){
                if(bodyPart.getVal() < BodyPart.BPEnd.getVal()){
                    charEquips.put(bodyPart,item.getItemId());
                }
                else if (bodyPart.getVal() > BodyPart.BPEnd.getVal() && bodyPart.getVal() != BodyPart.CashWeapon.getVal()){
                    charMaskedEquips.put(bodyPart,item.getItemId());
                }
                else if(bodyPart.getVal() == BodyPart.CashWeapon.getVal()){
                    cWeapon.add(item.getItemId());
                }
            }
        }
    }

    static boolean isBullet(int itemID) {
        return getItemPrefix(itemID) == 233;
    }

    static boolean isBowArrow(int itemID) {
        return itemID / 1000 == 2060;
    }

    static boolean isEnhancementScroll(int scrollID) {
        return scrollID / 100 == 20493;
    }

    static boolean isHat(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Hat.getVal();
    }

    static boolean isWeapon(int itemID) {
        return itemID >= 1210000 && itemID < 1600000;
    }

    static boolean isSecondary(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.SecondaryWeapon.getVal();
    }

    static boolean isShield(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Shield.getVal();
    }

    static boolean isAccessory(int itemID) {
        return (itemID >= 1010000 && itemID < 1040000) || (itemID >= 1122000 && itemID < 1153000) ||
                (itemID >= 1112000 && itemID < 1113000) || (itemID >= 1670000 && itemID < 1680000);
    }

    static boolean isFaceAccessory(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.FaceAccessory.getVal();
    }

    static boolean isEyeAccessory(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.EyeAccessory.getVal();
    }

    static boolean isEarrings(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Earrings.getVal();
    }

    static boolean isTop(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Top.getVal();
    }

    static boolean isOverall(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Overall.getVal();
    }

    static boolean isBottom(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Bottom.getVal();
    }

    static boolean isShoe(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Shoes.getVal();
    }

    static boolean isGlove(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Gloves.getVal();
    }

    static boolean isCape(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Cape.getVal();
    }

    static boolean isRing(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Ring.getVal();
    }

    static boolean isPendant(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Pendant.getVal();
    }

    static boolean isBelt(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Belt.getVal();
    }

    static boolean isMedal(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Medal.getVal();
    }

    static boolean isShoulder(int itemID) {
        return getItemPrefix(itemID) == EquipPrefix.Shoulder.getVal();
    }

    static boolean isArmor(int itemID) {
        return !isAccessory(itemID) && !isWeapon(itemID);
    }

    static boolean isThrowingItem(int itemID) {
        return isThrowingStar(itemID) || isBullet(itemID) || isBowArrow(itemID);
    }

    static boolean isThrowingStar(int itemID) {
        return getItemPrefix(itemID) == 207;
    }

    public static boolean canEquipTypeHavePotential(int itemId) {
        return isRing(itemId) ||
                isPendant(itemId) ||
                isWeapon(itemId) ||
                isBelt(itemId) ||
                isHat(itemId) ||
                isFaceAccessory(itemId) ||
                isEyeAccessory(itemId) ||
                isOverall(itemId) ||
                isTop(itemId) ||
                isBottom(itemId) ||
                isShoe(itemId) ||
                isEarrings(itemId) ||
                isShoulder(itemId) ||
                isGlove(itemId) ||
                isShield(itemId) ||
                isCape(itemId);
    }
}
