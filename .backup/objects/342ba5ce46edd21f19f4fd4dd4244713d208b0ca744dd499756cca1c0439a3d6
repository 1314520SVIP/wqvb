package com.example.textadventure;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GameEngine {
    private Player player;
    private Map<String, Room> rooms;
    private Room currentRoom;
    
    public GameEngine() {
        player = new Player();
        createRooms();
    }
    
    private void createRooms() {
        rooms = new HashMap<>();
        
        Room startRoom = new Room("起始房间", "这是一个简单的起始房间，有一扇门通向北边。");
        Room garden = new Room("花园", "这是一个美丽的花园，有各种颜色的花朵。");
        Room library = new Room("图书馆", "一个安静的图书馆，有很多书籍。");
        Room dungeon = new Room("地牢", "阴暗潮湿的地牢，似乎有什么在角落里闪闪发光。");
        Room treasureRoom = new Room("宝藏室", "金光闪闪的宝藏室，到处都是金币和宝石！");
        
        // 连接房间
        startRoom.setExit("北", garden);
        startRoom.setExit("东", dungeon);
        garden.setExit("南", startRoom);
        garden.setExit("东", library);
        library.setExit("西", garden);
        dungeon.setExit("西", startRoom);
        dungeon.setExit("北", treasureRoom);
        treasureRoom.setExit("南", dungeon);
        
        rooms.put("起始房间", startRoom);
        rooms.put("花园", garden);
        rooms.put("图书馆", library);
        rooms.put("地牢", dungeon);
        rooms.put("宝藏室", treasureRoom);
        
        currentRoom = startRoom;
    }
    
    public String startGame() {
        StringBuilder sb = new StringBuilder();
        sb.append("欢迎来到文字冒险游戏！\n");
        sb.append("你可以输入 '北', '南', '东', '西' 来移动\n");
        sb.append("输入 '退出' 来结束游戏\n");
        sb.append("输入 '查看' 来查看当前房间\n");
        sb.append("输入 '帮助' 来查看可用命令\n\n");
        sb.append(getCurrentLocationDescription());
        
        return sb.toString();
    }
    
    public String processCommand(String command) {
        command = command.trim();
        
        if(command.equals("退出")) {
            return "感谢游玩！";
        } else if(command.equals("查看")) {
            return getCurrentLocationDescription();
        } else if(command.equals("帮助")) {
            return getHelp();
        } else if(isMovementCommand(command)) {
            return move(command);
        } else {
            return "我无法理解你输入的命令。输入'帮助'来查看可用命令。";
        }
    }
    
    private boolean isMovementCommand(String command) {
        return command.equals("北") || command.equals("南") || command.equals("东") || command.equals("西");
    }
    
    private String move(String direction) {
        Room nextRoom = currentRoom.getExit(direction);
        if(nextRoom != null) {
            currentRoom = nextRoom;
            return getCurrentLocationDescription();
        } else {
            return "你不能往那个方向走。";
        }
    }
    
    private String getCurrentLocationDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- " + currentRoom.getName() + " ---\n");
        sb.append(currentRoom.getDescription() + "\n");
        sb.append("可移动方向: ");
        
        List<String> exits = currentRoom.getExits();
        for(int i = 0; i < exits.size(); i++) {
            if(i > 0) sb.append(", ");
            sb.append(exits.get(i));
        }
        sb.append("\n");
        
        // 检查是否到达宝藏室，游戏胜利
        if(currentRoom.getName().equals("宝藏室")) {
            sb.append("\n恭喜！你找到了宝藏，游戏胜利！\n");
        }
        
        return sb.toString();
    }
    
    private String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("可用命令:\n");
        sb.append("  北/南/东/西 - 移动到不同房间\n");
        sb.append("  查看 - 查看当前房间信息\n");
        sb.append("  帮助 - 显示此帮助信息\n");
        sb.append("  退出 - 退出游戏\n");
        return sb.toString();
    }
    
    public String getGameState() {
        return getCurrentLocationDescription();
    }
}

class Player {
    private String name;
    private int health;
    private int score;
    
    public Player() {
        this.name = "冒险者";
        this.health = 100;
        this.score = 0;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}

class Room {
    private String name;
    private String description;
    private Map<String, Room> exits;
    
    public Room(String name, String description) {
        this.name = name;
        this.description = description;
        this.exits = new HashMap<>();
    }
    
    public void setExit(String direction, Room room) {
        exits.put(direction, room);
    }
    
    public Room getExit(String direction) {
        return exits.get(direction);
    }
    
    public List<String> getExits() {
        return new ArrayList<>(exits.keySet());
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
}
