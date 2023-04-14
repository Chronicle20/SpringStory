package com.dori.Dori90v.connection.packet.handlers;

import com.dori.Dori90v.Server;
import com.dori.Dori90v.client.MapleClient;
import com.dori.Dori90v.client.character.MapleAccount;
import com.dori.Dori90v.client.character.MapleChar;
import com.dori.Dori90v.connection.packet.Handler;
import com.dori.Dori90v.connection.packet.InPacket;
import com.dori.Dori90v.connection.packet.OutPacket;
import com.dori.Dori90v.connection.packet.packets.CLogin;
import com.dori.Dori90v.enums.JobType;
import com.dori.Dori90v.enums.ServiceType;
import com.dori.Dori90v.logger.Logger;
import com.dori.Dori90v.services.ServiceManager;
import com.dori.Dori90v.world.MapleWorld;
import com.dori.Dori90v.constants.ServerConstants;
import com.dori.Dori90v.enums.LoginType;
import com.dori.Dori90v.services.MapleCharService;
import com.dori.Dori90v.world.MigrateInUser;

import java.net.InetAddress;
import java.util.Optional;

import static com.dori.Dori90v.connection.packet.headers.InHeader.*;
import static com.dori.Dori90v.constants.GameConstants.AMOUNT_OF_CREATION_EQUIPS_FOR_CHAR;
import static com.dori.Dori90v.constants.ServerConstants.*;

public class LoginHandler {
    // Logger -
    private static final Logger logger = new Logger(LoginHandler.class);

    @Handler(op = CheckPassword)
    public static void handleLoginPassword(MapleClient c, InPacket inPacket) {
        // Get from the packet the username & password -
        String username = inPacket.decodeString();
        String password = inPacket.decodeString();
        byte[] macID = inPacket.decodeArr(16);
        int gameRoomClient = inPacket.decodeInt(); // idk?
        byte nGameStartMode = inPacket.decodeByte(); // idk?
        byte unk = inPacket.decodeByte(); // idk?
        byte unk2 = inPacket.decodeByte(); // idk?
        int partnerCode = inPacket.decodeInt(); // idk?
        // Is successful login -
        boolean isSuccess = false;
        // Login result -
        LoginType loginType = LoginType.NotRegistered;
        // Get from the DB the potential account -
        Optional<?> entity = ServiceManager.getService(ServiceType.Account).getEntityByName(username);
        MapleAccount account = null;
        // If there is an entity from the DB & it's a MapleAccount instance then verify if account -
        if (entity.isPresent() && entity.get() instanceof MapleAccount) {
            account = (MapleAccount) entity.get();
            if (account.getPassword().equals(password)) {
                // Is account is banned? -
                if (account.getBanExpireDate() != null && !account.getBanExpireDate().isExpired()) {
                    loginType = LoginType.Blocked;
                } else {
                    // Success -
                    isSuccess = true;
                    loginType = LoginType.Success;
                    // Set the current account to the client -
                    c.setAccount(account);
                }
            } else {
                // Wrong password -
                loginType = LoginType.IncorrectPassword;
            }
        }
        // Send to the client the result of checking password -
        c.write(CLogin.checkPasswordResult(isSuccess, loginType, account));
    }

    @Handler(op = WorldRequest)
    public static void handleWorldListRequest(MapleClient c, InPacket inPacket) {
        // TODO: figure how to Set different background images - (currently this thing don't work :( )
        //c.write(CLogin.changeWorldSelectBackgroundImg());

        for (MapleWorld world : Server.getWorlds()) {
            c.write(CLogin.sendWorldInformation(world, world.getWorldSelectMessages()));
        }
        c.write(CLogin.sendWorldInformationEnd());
        c.write(CLogin.sendLatestConnectedWorld(ServerConstants.DEFAULT_WORLD_ID));
        //c.write(CLogin.sendRecommendWorldMessage(ServerConstants.DEFAULT_WORLD_ID, ServerConstants.RECOMMEND_MSG));
    }

    @Handler(op = WorldInfoRequest)
    public static void handleWorldInfoRequest(MapleClient c, InPacket inPacket) {
        // TODO: figure how to Set different background images - (currently this thing don't work :( )
        //c.write(CLogin.changeWorldSelectBackgroundImg());

        for (MapleWorld world : Server.getWorlds()) {
            c.write(CLogin.sendWorldInformation(world, world.getWorldSelectMessages()));
        }
        c.write(CLogin.sendWorldInformationEnd());
        c.write(CLogin.sendLatestConnectedWorld(ServerConstants.DEFAULT_WORLD_ID));
        //c.write(CLogin.sendRecommendWorldMessage(ServerConstants.DEFAULT_WORLD_ID, ServerConstants.RECOMMEND_MSG));
    }

    @Handler(op = CheckUserLimit)
    public static void handleWorldStatusRequest(MapleClient c, InPacket inPacket) {
        //TODO: need to add handling for verifying amount of connected users to the world
        byte worldId = inPacket.decodeByte();
        byte unk = inPacket.decodeByte(); // idk what is it?
        // Send the current world status -
        c.write(CLogin.getCheckUserLimit(worldId));
    }

    @Handler(op = SelectWorld)
    public static void handleSelectWorld(MapleClient c, InPacket inPacket) {
        byte loginType = inPacket.decodeByte(); // LoginType | suppose to be 2?
        byte worldId = inPacket.decodeByte();
        byte channel = (byte) (inPacket.decodeByte() + 1); // They send the channel number is offset by 1
        byte code = 0; // success code
        // Get the relevant Maple World -
        MapleWorld currWorld = Server.getWorldById(worldId);

        if (currWorld != null && currWorld.getChannelById(channel) != null && loginType == 2) {
            c.setWorldId(worldId);
            c.setChannel(channel);
            c.setMapleChannelInstance(currWorld.getChannelById(channel));
            // Send select world result -
            c.write(CLogin.selectWorldResult(c.getAccount(), code));
        } else {
            // Close the session cause there is an issue for that client -
            c.close();
        }
    }

    @Handler(op = CheckDuplicatedID)
    public static void handleCheckCharacterName(MapleClient c, InPacket inPacket) {
        // Getting the chosen name from the client -
        String chosenName = inPacket.decodeString();
        // Verify if there is a char that use this name already -
        Optional<?> character = ServiceManager.getService(ServiceType.Character).getEntityByName(chosenName);
        boolean isUsed = character.isPresent() && character.get() instanceof MapleChar;
        // Send a response -
        c.write(CLogin.charNameResponse(chosenName, isUsed));
    }

    @Handler(op = CreateNewCharacter)
    public static void handleCharCreation(MapleClient c, InPacket inPacket) {
        String name = inPacket.decodeString();
        int jobType = inPacket.decodeInt();
        // Get job by job type -
        int job = JobType.getTypeByVal(jobType).getStartJobByType().getId();
        short subJob = inPacket.decodeShort();
        int[] charAppearance = new int[8];
        for (int i = 0; i < 8; i++) {
            // {0 - face, 1 - hairColor, 2 - hair, 3 - skinColor, 4 - top, 5 - bottom, 6 - shoes, 7 - weapon}
            charAppearance[i] = inPacket.decodeInt();
        }
        byte gender = inPacket.decodeByte();
        // LoginType -
        LoginType loginType = LoginType.TempBlocked;
        MapleChar newChar = null;
        // Verify if the name is valid to use -
        Optional<?> existingChar = ServiceManager.getService(ServiceType.Character).getEntityByName(name);
        if (existingChar.isEmpty()) {
            // Attempt to create a new character -
            newChar = new MapleChar(c.getAccount().getId(), name, gender, job, subJob, charAppearance);
            c.getAccount().getCharacters().add(newChar);
            if (newChar.getEquippedInventory().getItems().size() == AMOUNT_OF_CREATION_EQUIPS_FOR_CHAR) {
                ((MapleCharService) ServiceManager.getService(ServiceType.Character)).addNewEntity(newChar);
                loginType = LoginType.Success;
            }
        }
        // Send a new character creation successful creation -
        c.write(CLogin.createNewCharacterResult(loginType, newChar));
    }

    @Handler(op = SelectCharacter)
    public static void handleCharSelect(MapleClient c, InPacket inPacket) {
        int characterID = inPacket.decodeInt();
        String macID = inPacket.decodeString(); // hardware id
        String hwID = inPacket.decodeString(); // machine id
        try {
            byte[] clientMachineID = InetAddress.getByName(ServerConstants.HOST_IP).getAddress();
            c.setMachineID(clientMachineID);
            // Add Migrate in user for the server instance - (preparing for MigrateIn of a chosen character)
            MigrateInUser migrateInUser = new MigrateInUser(c.getAccount(), c.getMapleChannelInstance(), c.getWorldId(), c.getMachineID());
            Server.migrateInNewCharacter(c.getAccount().getId(), migrateInUser);
            // Send character select result -
            c.write(CLogin.onSelectCharacterResult(clientMachineID, c.getMapleChannelInstance().getPort(), characterID));
        } catch (Exception e) {
            logger.error("The server host IP is unknown?");
            e.printStackTrace();
            c.close();
        }
    }

    @Handler(op = CreateSecurityHandle)
    public static void handleCreateSecurityHandle(MapleClient c, InPacket inPacket) {
        // If it's true will auto login as admin -
        if (AUTO_LOGIN) {
            OutPacket outPacket = new OutPacket();
            outPacket.encodeString(AUTO_LOGIN_USERNAME);
            outPacket.encodeString(AUTO_LOGIN_PASSWORD);

            handleLoginPassword(c, new InPacket(outPacket.getData()));
        }
    }
}
