// Game Logic Ported from GameEngine.java
class Room {
    constructor(name, description) {
        this.name = name;
        this.description = description;
        this.exits = new Map();
    }

    setExit(direction, room) {
        this.exits.set(direction, room);
    }

    getExit(direction) {
        return this.exits.get(direction);
    }

    getExits() {
        return Array.from(this.exits.keys());
    }
}

class GameEngine {
    constructor() {
        this.rooms = new Map();
        this.currentRoom = null;
        this.createRooms();
    }

    createRooms() {
        const startRoom = new Room("起始房间", "这是一个简单的起始房间，有一扇门通向北边。");
        const garden = new Room("花园", "这是一个美丽的花园，有各种颜色的花朵。");
        const library = new Room("图书馆", "一个安静的图书馆，有很多书籍。");
        const dungeon = new Room("地牢", "阴暗潮湿的地牢，似乎有什么在角落里闪闪发光。");
        const treasureRoom = new Room("宝藏室", "金光闪闪的宝藏室，到处都是金币和宝石！");

        // Connect rooms
        startRoom.setExit("北", garden);
        startRoom.setExit("东", dungeon);
        garden.setExit("南", startRoom);
        garden.setExit("东", library);
        library.setExit("西", garden);
        dungeon.setExit("西", startRoom);
        dungeon.setExit("北", treasureRoom);
        treasureRoom.setExit("南", dungeon);

        this.rooms.set("起始房间", startRoom);
        this.rooms.set("花园", garden);
        this.rooms.set("图书馆", library);
        this.rooms.set("地牢", dungeon);
        this.rooms.set("宝藏室", treasureRoom);

        this.currentRoom = startRoom;
    }

    startGame() {
        return "欢迎来到文字冒险游戏！\n" +
               "你可以输入 '北', '南', '东', '西' 来移动\n" +
               "输入 '退出' 来结束游戏\n" +
               "输入 '查看' 来查看当前房间\n" +
               "输入 '帮助' 来查看可用命令\n\n" +
               this.getCurrentLocationDescription();
    }

    processCommand(command) {
        command = command.trim();

        if (command === "退出") {
            return "感谢游玩！";
        } else if (command === "查看") {
            return this.getCurrentLocationDescription();
        } else if (command === "帮助") {
            return this.getHelp();
        } else if (this.isMovementCommand(command)) {
            return this.move(command);
        } else {
            return "我无法理解你输入的命令。输入'帮助'来查看可用命令。";
        }
    }

    isMovementCommand(command) {
        return ["北", "南", "东", "西"].includes(command);
    }

    move(direction) {
        const nextRoom = this.currentRoom.getExit(direction);
        if (nextRoom) {
            this.currentRoom = nextRoom;
            return this.getCurrentLocationDescription();
        } else {
            return "你不能往那个方向走。";
        }
    }

    getCurrentLocationDescription() {
        let sb = `--- ${this.currentRoom.name} ---\n`;
        sb += `${this.currentRoom.description}\n`;
        sb += "可移动方向: ";

        const exits = this.currentRoom.getExits();
        sb += exits.join(", ");
        sb += "\n";

        if (this.currentRoom.name === "宝藏室") {
            sb += "\n恭喜！你找到了宝藏，游戏胜利！\n";
        }

        return sb;
    }

    getHelp() {
        return "可用命令:\n" +
               "  北/南/东/西 - 移动到不同房间\n" +
               "  查看 - 查看当前房间信息\n" +
               "  帮助 - 显示此帮助信息\n" +
               "  退出 - 退出游戏\n";
    }

    getGameState() {
        return this.getCurrentLocationDescription();
    }
}

// UI Controller
const game = new GameEngine();
const outputDiv = document.getElementById('output');
const inputField = document.getElementById('command-input');
const submitBtn = document.getElementById('submit-btn');

function appendText(text, className) {
    const span = document.createElement('span');
    span.className = className || '';
    span.textContent = text;
    outputDiv.appendChild(span);
    outputDiv.scrollTop = outputDiv.scrollHeight;
}

function handleCommand() {
    const command = inputField.value.trim();
    if (!command) return;

    appendText(`\n> ${command}\n`, 'command-line');
    
    const result = game.processCommand(command);
    
    // Parse result to highlight room names
    const lines = result.split('\n');
    lines.forEach(line => {
        if (line.startsWith('--- ')) {
            appendText(line + '\n', 'room-name');
        } else if (line.includes('感谢游玩') || line.includes('游戏胜利')) {
            appendText(line + '\n', 'system-message');
        } else {
            appendText(line + '\n', '');
        }
    });

    inputField.value = '';
}

// Initialize
appendText(game.startGame(), '');

// Event Listeners
submitBtn.addEventListener('click', handleCommand);
inputField.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') handleCommand();
});
