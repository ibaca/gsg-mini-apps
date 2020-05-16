package maze.client;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.gwt.elemento.core.Elements.canvas;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.option;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.select;
import static org.jboss.gwt.elemento.core.InputType.button;

import com.google.gwt.core.client.EntryPoint;
import com.google.web.bindery.event.shared.HandlerRegistration;
import elemental2.dom.CSSProperties.OpacityUnionType;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLSelectElement;
import elemental2.dom.Image;
import elemental2.dom.ImageData;
import elemental2.dom.KeyboardEvent;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.IntConsumer;
import jsinterop.base.Js;
import maze.client.Maze.Board.Cell;
import maze.client.Maze.Board.Dir;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventType;

public class Maze implements EntryPoint {

    @Override
    public void onModuleLoad() {
        final HTMLDivElement page;
        final HTMLDivElement messageContainer;
        final HTMLSelectElement diffSelect;
        final HTMLInputElement startMazeBtn;
        final HTMLDivElement view;
        final HTMLDivElement mazeContainer;
        final HTMLCanvasElement mazeCanvas;
        final HTMLInputElement okBtn;

        document.body.appendChild(div().id("gradient").get());
        document.body.appendChild(page = div().id("page").get());
        page.appendChild(messageContainer = div().id("messageContainer")
                .add(div().id("message")
                        .add(h(1).textContent("Congratulations!"))
                        .add(p().textContent("You are done."))
                        .add(p().id("moves").get())
                        .add(okBtn = input(button).id("okBtn").apply(el -> el.value = "Cool!").get())
                        .get())
                .get());
        page.appendChild(div().id("menu")
                .add(div().css("custom-select")
                        .add(diffSelect = select().id("diffSelect")
                                .add(option("Easy").apply(el -> el.value = "10"))
                                .add(option("Medium").apply(el -> el.value = "15"))
                                .add(option("Hard").apply(el -> el.value = "25"))
                                .add(option("Extreme").apply(el -> el.value = "38"))
                                .get()))
                .add(startMazeBtn = input(button).id("startMazeBtn").apply(el -> el.value = "Start").get())
                .get());
        page.appendChild(view = div().id("view")
                .add(mazeContainer = div().id("mazeContainer")
                        .add(mazeCanvas = canvas().id("mazeCanvas").css("border").apply(el -> {
                            el.height = 1100;
                            el.width = 1100;
                        }).get())
                        .get())
                .get());

        class State {
            CanvasRenderingContext2D ctx = Js.cast(mazeCanvas.getContext("2d"));
            Image playerSprite;
            Image finishSprite;
            Board maze;
            DrawMaze draw;
            Player player;
            double cellSize;
            int difficulty;

            public void makeMaze() {
                if (player != null) {
                    player.unbindKeyDown();
                    player = null;
                }
                difficulty = Js.coerceToInt(diffSelect.options.getAt(diffSelect.selectedIndex).value);
                cellSize = mazeCanvas.width / (double) difficulty;
                maze = new Board(difficulty, difficulty);
                draw = new DrawMaze(maze, ctx, cellSize, finishSprite);
                player = new Player(maze, ctx, cellSize, Maze::displayVictoryMess, playerSprite);
                if (Js.coerceToDouble(mazeContainer.style.opacity) < 100) {
                    mazeContainer.style.opacity = OpacityUnionType.of(100);
                }
            }
        }

        State state = new State();

        Runnable resetSize = () -> {
            int viewWidth = view.offsetWidth;
            int viewHeight = view.offsetHeight;
            if (viewHeight < viewWidth) {
                state.ctx.canvas.width = viewHeight - viewHeight / 100;
                state.ctx.canvas.height = viewHeight - viewHeight / 100;
            } else {
                state.ctx.canvas.width = viewWidth - viewWidth / 100;
                state.ctx.canvas.height = viewWidth - viewWidth / 100;
            }
        };

        {//window.onload: entry point is already executed onload
            resetSize.run();

            // Load and edit sprites
            Runnable isComplete = () -> {
                if (state.playerSprite != null && state.finishSprite != null) {
                    DomGlobal.setTimeout(ev -> state.makeMaze(), 500);
                }
            };

            Image sprite = new Image();
            sprite.src = "https://image.ibb.co/dr1HZy/Pf_RWr3_X_Imgur.png?" + new Date().getTime();
            sprite.setAttribute("crossOrigin", " ");
            sprite.onload = ev -> {
                state.playerSprite = changeBrightness(1.2, sprite);
                isComplete.run();
                return null;
            };

            Image finishSprite = new Image();
            finishSprite.src = "https://image.ibb.co/b9wqnJ/i_Q7m_U25_Imgur.png?" + new Date().getTime();
            finishSprite.setAttribute("crossOrigin", " ");
            finishSprite.onload = ev -> {
                state.finishSprite = changeBrightness(1.1, finishSprite);
                isComplete.run();
                return null;
            };

            EventType.bind(okBtn, EventType.click, ev -> toggleVisibility(messageContainer));
            EventType.bind(startMazeBtn, EventType.click, ev -> state.makeMaze());
        }

        window.onresize = ev -> {
            resetSize.run();
            state.cellSize = mazeCanvas.width / (double) state.difficulty;
            if (state.player != null) {
                state.draw.redrawMaze(state.cellSize);
                state.player.redrawPlayer(state.cellSize);
            }
            return null;
        };
    }

    public static int rand(int max) {
        return (int) Math.floor(Math.random() * max);
    }

    public static <T> void shuffle(T[] in) {
        for (int i = in.length - 1; i > 0; i--) {
            int j = (int) Math.floor(Math.random() * (i + 1));
            T tmp = in[i];
            in[i] = in[j];
            in[j] = tmp;
        }
    }

    public static Image changeBrightness(double factor, HTMLImageElement sprite) {
        HTMLCanvasElement tmp = Elements.canvas().get();
        tmp.width = 500;
        tmp.height = 500;

        CanvasRenderingContext2D context = Js.cast(tmp.getContext("2d"));
        context.drawImage(sprite, 0, 0, 500, 500);

        ImageData imgData = context.getImageData(0, 0, 500, 500);
        for (int i = 0; i < imgData.data.length; i += 4) {
            imgData.data.setAt(i, imgData.data.getAt(i) * factor);
            imgData.data.setAt(i + 1, imgData.data.getAt(i + 1) * factor);
            imgData.data.setAt(i + 2, imgData.data.getAt(i + 2) * factor);
        }
        context.putImageData(imgData, 0, 0);

        Image spriteOutput = new Image();
        spriteOutput.src = tmp.toDataURL(null);
        tmp.remove();
        return spriteOutput;
    }

    public static void displayVictoryMess(int moves) {
        document.getElementById("moves").innerHTML = "You Moved " + moves + " Steps.";
        toggleVisibility((HTMLElement) document.getElementById("messageContainer"));
    }

    public static void toggleVisibility(HTMLElement el) {
        el.style.visibility = Objects.equals(el.style.visibility, "visible") ? "hidden" : "visible";
    }

    public static class Board {
        Cell[][] map;
        int width, height;

        enum Dir { //@formatter:off
            N(-1, 0) {Dir o() { return S; }},
            S(+1, 0) {Dir o() { return N; }},
            E(0, +1) {Dir o() { return W; }},
            W(0, -1) {Dir o() { return E; }};
            final int x, y;
            Dir(int y, int x) { this.x = x;this.y = y; }
            abstract Dir o();
        } //@formatter:on

        XY startCoord, endCoord;

        public Board(int width, int height) {
            this.width = width;
            this.height = height;
            genMap();
            defineStartEnd();
            defineMaze();
        }

        void genMap() {
            map = new Cell[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; ++x) {
                    map[y][x] = new Cell();
                }
            }
        }

        void defineMaze() {
            boolean isComp = false;
            boolean move;
            int cellsVisited = 1;
            int numLoops = 0;
            int maxLoops = 0;
            XY pos = new XY(0, 0);
            int numCells = width * height;
            Dir[] dirs = Dir.values();
            while (!isComp) {
                move = false;
                map[pos.x][pos.y].visited = true;

                if (numLoops >= maxLoops) {
                    shuffle(dirs);
                    maxLoops = Math.round(rand(height / 8));
                    numLoops = 0;
                }
                numLoops++;
                for (Dir direction : dirs) {
                    int nx = pos.x + direction.x;
                    int ny = pos.y + direction.y;

                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        //Check if the tile is already visited
                        if (!map[nx][ny].visited) {
                            //Carve through walls from this tile to next
                            map[pos.x][pos.y].value.add(direction);
                            map[nx][ny].value.add(direction.o());

                            //Set current cell as next cells Prior visited
                            map[nx][ny].priorPos = pos;
                            //Update Cell position to newly visited location
                            pos = new XY(nx, ny);
                            cellsVisited++;
                            //Recursively call this method on the next tile
                            move = true;
                            break;
                        }
                    }
                }

                //  If it failed to find a direction, move the current position back to the prior cell and Recall the method
                if (!move) pos = map[pos.x][pos.y].priorPos;

                if (numCells == cellsVisited) isComp = true;
            }
        }

        void defineStartEnd() {
            switch (rand(4)) {
                case 0:
                    startCoord = new XY(0, 0);
                    endCoord = new XY(height - 1, width - 1);
                    break;
                case 1:
                    startCoord = new XY(0, width - 1);
                    endCoord = new XY(height - 1, 0);
                    break;
                case 2:
                    startCoord = new XY(height - 1, 0);
                    endCoord = new XY(0, width - 1);
                    break;
                case 3:
                    startCoord = new XY(height - 1, width - 1);
                    endCoord = new XY(0, 0);
                    break;
            }
        }

        static class Cell {
            boolean visited;
            final EnumSet<Dir> value = EnumSet.noneOf(Dir.class);
            XY priorPos;
        }
    }

    static class DrawMaze {
        final Board maze;
        final CanvasRenderingContext2D ctx;
        double cellSize;
        final Image endSprite;

        public DrawMaze(Board maze, CanvasRenderingContext2D ctx, double cellSize, Image endSprite) {
            this.maze = maze;
            this.ctx = ctx;
            this.cellSize = cellSize;
            this.endSprite = endSprite;
            ctx.lineWidth = cellSize / 40.;

            clear();
            drawMap();
            drawEnd();
        }

        void redrawMaze(double size) {
            cellSize = size;
            ctx.lineWidth = cellSize / 50.;
            drawMap();
            drawEnd();
        }

        void drawCell(int xCord, int yCord, Cell cell) {
            double x = xCord * cellSize;
            double y = yCord * cellSize;

            if (!cell.value.contains(Dir.N)) {
                ctx.beginPath();
                ctx.moveTo(x, y);
                ctx.lineTo(x + cellSize, y);
                ctx.stroke();
            }
            if (!cell.value.contains(Dir.S)) {
                ctx.beginPath();
                ctx.moveTo(x, y + cellSize);
                ctx.lineTo(x + cellSize, y + cellSize);
                ctx.stroke();
            }
            if (!cell.value.contains(Dir.E)) {
                ctx.beginPath();
                ctx.moveTo(x + cellSize, y);
                ctx.lineTo(x + cellSize, y + cellSize);
                ctx.stroke();
            }
            if (!cell.value.contains(Dir.W)) {
                ctx.beginPath();
                ctx.moveTo(x, y);
                ctx.lineTo(x, y + cellSize);
                ctx.stroke();
            }
        }

        void drawMap() {
            for (int x = 0; x < maze.map.length; x++) {
                for (int y = 0; y < maze.map[x].length; y++) {
                    drawCell(x, y, maze.map[x][y]);
                }
            }
        }

        void drawEnd() {
            double offsetLeft = cellSize / 50.;
            double offsetRight = cellSize / 25.;
            XY coord = maze.endCoord;
            ctx.drawImage(
                    endSprite,
                    2,
                    2,
                    endSprite.width,
                    endSprite.height,
                    coord.x * cellSize + offsetLeft,
                    coord.y * cellSize + offsetLeft,
                    cellSize - offsetRight,
                    cellSize - offsetRight
            );
        }

        void clear() {
            double canvasSize = cellSize * maze.map.length;
            ctx.clearRect(0, 0, canvasSize, canvasSize);
        }
    }

    static class Player {
        final Board maze;
        final CanvasRenderingContext2D ctx;
        final Cell[][] map;
        XY cellCoords;
        int moves;
        double cellSize;
        final IntConsumer onComplete;
        final Image sprite;
        private HandlerRegistration keyDownHandler;

        public Player(Board maze, CanvasRenderingContext2D ctx, double cs, IntConsumer onComplete, Image sprite) {
            this.maze = maze;
            this.map = maze.map;
            this.ctx = ctx;
            this.cellCoords = new XY(maze.startCoord.x, maze.startCoord.y);
            this.cellSize = cs;
            this.onComplete = onComplete;
            this.sprite = sprite;

            drawSprite(maze.startCoord);
            bindKeyDown();
        }

        void redrawPlayer(double cs) {
            cellSize = cs;
            drawSprite(cellCoords);
        }

        void drawSprite(XY coord) {
            double offsetLeft = cellSize / 50.;
            double offsetRight = cellSize / 25.;
            ctx.drawImage(
                    sprite,
                    0,
                    0,
                    sprite.width,
                    sprite.height,
                    coord.x * cellSize + offsetLeft,
                    coord.y * cellSize + offsetLeft,
                    cellSize - offsetRight,
                    cellSize - offsetRight
            );
            if (coord.x == maze.endCoord.x && coord.y == maze.endCoord.y) {
                onComplete.accept(moves);
                unbindKeyDown();
            }
        }

        void removeSprite(XY coord) {
            double offsetLeft = cellSize / 50.;
            double offsetRight = cellSize / 25.;
            ctx.clearRect(
                    coord.x * cellSize + offsetLeft,
                    coord.y * cellSize + offsetLeft,
                    cellSize - offsetRight,
                    cellSize - offsetRight
            );
        }

        void check(KeyboardEvent e) {
            Cell cell = map[cellCoords.x][cellCoords.y];
            moves++;
            switch (e.key) {
                case "KeyA":
                case "ArrowLeft": // west
                    if (cell.value.contains(Dir.W)) {
                        removeSprite(cellCoords);
                        cellCoords = new XY(cellCoords.x - 1, cellCoords.y);
                        drawSprite(cellCoords);
                    }
                    break;
                case "KeyW":
                case "ArrowUp": // north
                    if (cell.value.contains(Dir.N)) {
                        removeSprite(cellCoords);
                        cellCoords = new XY(cellCoords.x, cellCoords.y - 1);
                        drawSprite(cellCoords);
                    }
                    break;
                case "KeyD":
                case "ArrowRight": // east
                    if (cell.value.contains(Dir.E)) {
                        removeSprite(cellCoords);
                        cellCoords = new XY(cellCoords.x + 1, cellCoords.y);
                        drawSprite(cellCoords);
                    }
                    break;
                case "KeyS":
                case "ArrowDown": // south
                    if (cell.value.contains(Dir.S)) {
                        removeSprite(cellCoords);
                        cellCoords = new XY(cellCoords.x, cellCoords.y + 1);
                        drawSprite(cellCoords);
                    }
                    break;
            }
        }

        void bindKeyDown() {
            // WARN! the method reference cannot be used to remove the listener, always use the bind reg. handler!
            if (keyDownHandler == null) keyDownHandler = EventType.bind(window, EventType.keydown, false, this::check);
        }

        void unbindKeyDown() {
            if (keyDownHandler != null) keyDownHandler.removeHandler();
        }
    }

    static class XY {
        int x, y;
        public XY(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
