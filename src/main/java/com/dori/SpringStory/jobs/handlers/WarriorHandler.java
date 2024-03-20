package com.dori.SpringStory.jobs.handlers;

import com.dori.SpringStory.client.character.MapleChar;
import com.dori.SpringStory.client.character.Skill;
import com.dori.SpringStory.dataHandlers.SkillDataHandler;
import com.dori.SpringStory.enums.ChatType;
import com.dori.SpringStory.enums.SkillStat;
import com.dori.SpringStory.enums.Skills;
import com.dori.SpringStory.jobs.JobHandler;
import com.dori.SpringStory.logger.Logger;
import com.dori.SpringStory.temporaryStats.characters.CharacterTemporaryStat;
import com.dori.SpringStory.utils.FormulaCalcUtils;
import com.dori.SpringStory.utils.MapleUtils;
import com.dori.SpringStory.dataHandlers.dataEntities.SkillData;
import lombok.NoArgsConstructor;

import static com.dori.SpringStory.enums.SkillStat.prop;
import static com.dori.SpringStory.enums.SkillStat.x;
import static com.dori.SpringStory.enums.Skills.DRAGONKNIGHT_DRAGON_ROAR;
import static com.dori.SpringStory.utils.MapleUtils.getPercentageOf;

@NoArgsConstructor
public class WarriorHandler implements JobHandler {
    private static final Logger logger = new Logger(WarriorHandler.class);

    private static WarriorHandler instance;

    public static WarriorHandler getInstance() {
        if (instance == null) {
            instance = new WarriorHandler();
        }
        return instance;
    }

    private void handleHpRecovery(MapleChar chr, SkillData skillData, int slv) {
        int percentageToHeal = FormulaCalcUtils.calcValueFromFormula(skillData.getSkillStatInfo().get(SkillStat.x), slv);
        int amountToHeal = chr.getMaxHp() * percentageToHeal / 100;
        chr.modifyHp(amountToHeal);
    }

    public void handleDarkKnightHpConsumption(SkillData skillData, int skillID, int slv, MapleChar chr) {
        if (skillID == Skills.DRAGONKNIGHT_SACRIFICE.getId() || skillID == DRAGONKNIGHT_DRAGON_ROAR.getId()) {
            int amountToConsume;
            // wz base handling for the skill -
            String hpConsumptionFormula = skillData.getSkillStatInfo().getOrDefault(x, "");
            amountToConsume = FormulaCalcUtils.calcValueFromFormula(hpConsumptionFormula, slv);
            if (amountToConsume != 0) {
                chr.modifyHp(-(amountToConsume * chr.getHp() / 100));
            }
        }
    }

    public boolean isDragonSkill(int skillID) {
        return skillID == DRAGONKNIGHT_DRAGON_ROAR.getId()
                || skillID == Skills.DRAGONKNIGHT_DRAGON_FURY.getId()
                || skillID == Skills.DRAGONKNIGHT_DRAGON_BURSTER.getId()
                || skillID == Skills.DRAGONKNIGHT_SACRIFICE.getId();
    }

    public void handleDarkKnightDragonSkillHealthRegen(MapleChar chr, int skillID, int dmg) {
        Skill dragonWisdom = chr.getSkill(Skills.DRAGONKNIGHT_DRAGON_WISDOM.getId());
        if (isDragonSkill(skillID) && dragonWisdom != null) {
            SkillData skillData = SkillDataHandler.getSkillDataByID(dragonWisdom.getSkillId());
            int chanceToRegen = FormulaCalcUtils.calcValueFromFormula(skillData.getSkillStatInfo().get(prop), dragonWisdom.getCurrentLevel());
            int percentageOfHealthToRegen = FormulaCalcUtils.calcValueFromFormula(skillData.getSkillStatInfo().get(x), dragonWisdom.getCurrentLevel());
            if (MapleUtils.succeedProp(chanceToRegen)) {
                int amountOfHealthToRegen = getPercentageOf(dmg, percentageOfHealthToRegen);
                int healthRegenCap = getPercentageOf(chr.getMaxHp(), 50);
                chr.modifyHp(Math.min(amountOfHealthToRegen, healthRegenCap));
            }
        }
    }

    private void handleDivineShield(MapleChar chr, SkillData skillData, int slv) {
        if (chr.getSkill(Skills.PALADIN_DIVINE_SHIELD.getId()) != null) {
            int percentageToActivateShield = FormulaCalcUtils.calcValueFromFormula(skillData.getSkillStatInfo().get(SkillStat.prop), slv);
            int maxAmountOfAbsorbedHits = FormulaCalcUtils.calcValueFromFormula(skillData.getSkillStatInfo().get(SkillStat.x), slv);
            int durationInSec = FormulaCalcUtils.calcValueFromFormula(skillData.getSkillStatInfo().get(SkillStat.time), slv);
            int amountOfAbsorbedHits = chr.getTsm().getCTS(CharacterTemporaryStat.BlessingArmor);
            if (amountOfAbsorbedHits > 0) {
                if (amountOfAbsorbedHits < maxAmountOfAbsorbedHits) {
                    chr.getTsm().addTempStat(CharacterTemporaryStat.BlessingArmor, skillData.getSkillId(), (amountOfAbsorbedHits + 1), durationInSec);
                } else {
                    //reset! + cooldown -
                    chr.cancelBuff(skillData.getSkillId());
//                    chr.getTsm().markExpiredStat(skillData.getSkillId());
//                    chr.resetTemporaryStats();
                    //TODO: need to handle cooldowns!
                }
            } else if (MapleUtils.succeedProp(percentageToActivateShield)) {
                int pad = FormulaCalcUtils.calcValueFromFormula(skillData.getSkillStatInfo().get(SkillStat.epad), slv);
                chr.getTsm().addTempStat(CharacterTemporaryStat.BlessingArmor, skillData.getSkillId(), 1, durationInSec);
                chr.getTsm().addTempStat(CharacterTemporaryStat.Pad, skillData.getSkillId(), pad, durationInSec);
                chr.applyTemporaryStats();
            }
        }
    }

    @Override
    public boolean handleSkill(MapleChar chr, SkillData skillData, int slv) {
        Skills skill = Skills.getSkillById(skillData.getSkillId());
        switch (skill) {
            case WHITE_KNIGHT_HP_RECOVERY -> handleHpRecovery(chr, skillData, slv);
            case PALADIN_DIVINE_SHIELD -> handleDivineShield(chr, skillData, slv);
            default -> {
                logger.warning("The Skill: " + skillData.getSkillId() + ", isn't handle by the WarriorHandler!");
                return false;
            }
        }
        return true;
    }
}
