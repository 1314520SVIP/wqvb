import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Game {
    private Scanner scanner;
    private Player player;
    private Map<String, Room> rooms;
    private Room currentRoom;
    
    public Game() {
        scanner = new Scanner(System.in);
        player = new Player();
        createRooms();
    }
    
    private void createRooms() {
        rooms = new HashMap<>();
        
        Room startRoom = new Room("起始房间", "这是一个简单的起始房间，有一扇门通向北边。");
        Room garden = new Room("花园", "这是一个美丽的花园，有各种颜色的花朵。");
        Room library = new Room("图书馆", "一个安静的图书馆，有很多书籍。");
        
        startRoom.setExit("北", garden);
        garden.setExit("南", startRoom);
        garden.setExit("东", library);
        library.setExit("西", garden);
        
        rooms.put("起始房间", startRoom);
        rooms.put("花园", garden);
        rooms.put("图书馆", library);
        
        currentRoom = startRoom;
    }
    
    public void play() {
        System.out.println("欢迎来到文字冒险游戏！");
        System.out.println("你可以输入 '北', '南', '东', '西' 来移动");
        System.out.println("输入 '退出' 来结束游戏");
        System.out.println("输入 '查看' 来查看当前房间");
        System.out.println("输入 '帮助' 来查看可用命令");
        
        printCurrentLocation();
        
        while(true) {
            System.out.print("\n> ");
            String command = scanner.nextLine().trim();
            
            if(command.equals("退出")) {
                System.out.println("感谢游玩！");
                break;
            } else if(command.equals("查看")) {
                printCurrentLocation();
            } else if(command.equals("帮助")) {
                showHelp();
            } else if(command.equals("北") || command.equals("南") || command.equals("东") || command.equals("西")) {
                move(command);
            } else {
                System.out.println("我无法理解你输入的命令。输入'帮助'来查看可用命令。");
            }
        }
    }
    
    private void move(String direction) {
        Room nextRoom = currentRoom.getExit(direction);
        if(nextRoom != null) {
            currentRoom = nextRoom;
            printCurrentLocation();
        } else {
            System.out.println("你不能往那个方向走。");
        }
    }
    
    private void printCurrentLocation() {
        System.out.println("\n--- " + currentRoom.getName() + " ---");
        System.out.println(currentRoom.getDescription());
        System.out.print("可移动方向: ");
        List<String> exits = currentRoom.getExits();
        for(int i = 0; i < exits.size(); i++) {
            if(i > 0) System.out.print(", ");
            System.out.print(exits.get(i));
        }
        System.out.println();
    }
    
    private void showHelp() {
        System.out.println("可用命令:");
        System.out.println("  北/南/东/西 - 移动到不同房间");
        System.out.println("  查看 - 查看当前房间信息");
        System.out.println("  帮助 - 显示此帮助信息");
        System.out.println("  退出 - 退出游戏");
    }
    
    public static void main(String[] args) {
        Game game = new Game();
        game.play();
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
