import java.util.*;
import java.io.*;

public class Main {
    static class Fire {
        int r, c, d, v;

        public Fire (int r, int c, int d, int v) {
            this.r = r;
            this.c = c;
            this.d = d;
            this.v = v;
        }
    }

    public static final int EAST = 0;
    public static final int WEST = 1;
    public static final int SOUTH = 2;
    public static final int NORTH = 3;
    public static final int TOP = 4;
    public static final int BOTTOM = 5;

    public static final int[][] nextPos = {{NORTH, SOUTH, BOTTOM, TOP},
                                        {SOUTH, NORTH, BOTTOM , TOP},
                                        {EAST, WEST, BOTTOM, TOP},
                                        {WEST, EAST, BOTTOM, TOP},
                                        {EAST, WEST, SOUTH, NORTH}};

    public static int[][] deltas = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    public static int N, M, time;
    public static int[] machine;
    public static int[][] floor;
    public static int[][][] wall;
    public static Queue<Fire> fires;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        int F = Integer.parseInt(st.nextToken());

        floor = new int[N][N];
        int[] exit = new int[2];

        int[] wallStart = new int[] {-1, -1};

        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());

            for (int j = 0; j < N; j++) {
                floor[i][j] = Integer.parseInt(st.nextToken());

                if (floor[i][j] == 4) {
                    exit[0] = i;
                    exit[1] = j;
                } else if (floor[i][j] == 3 && wallStart[0] == -1) {
                    wallStart[0] = i;
                    wallStart[1] = j;
                }
            }
        }

        wall = new int[6][M][M];
        int[] start = new int[]{-1, -1};

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (floor[i][j] == 0) {
                    for (int d = 0; d < 4; d++) {
                        int nr = i + deltas[d][0];
                        int nc = j + deltas[d][1];

                        if (isIn(nr, nc, N) && floor[nr][nc] == 3) {
                            start[0] = i;
                            start[1] = j;

                            wall[5][nr - wallStart[0]][nc - wallStart[1]] = -1;

                            break;
                        }
                    }
                }
            }
        }

        if (start[0] == -1) {
            start[0] = exit[0];
            start[1] = exit[1];

            wall[5][exit[0] - wallStart[0]][exit[1] - wallStart[1]] = -1;
        }

        machine = new int[3];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < M; j++) {
                st = new StringTokenizer(br.readLine());
                for (int k = 0; k < M; k++) {
                    wall[i][j][k] = Integer.parseInt(st.nextToken());

                    if (wall[i][j][k] == 2) {
                        machine[0] = TOP;
                        machine[1] = j;
                        machine[2] = k;
                    }
                }
            }
        }

        fires = new ArrayDeque();

        for (int i = 0; i < F; i++) {
            st = new StringTokenizer(br.readLine());

            int r = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());
            int v = Integer.parseInt(st.nextToken());

            fires.offer(new Fire(r, c, d, v));
            floor[r][c] = 1;
        }

        // 1. 시간의 벽 탈출
        time = moveInWall();

        if (time == -1) {
            System.out.println(-1);
        } else {
            // 2. 시간의 벽을 탈출하는 동안 시간 이상 현상 처리
            for (int i = 1; i <= time; i++) {
                spreadTime(i);
            }

            time++;

            // 3. 미지의 공간에서 탈출
            int result = moveInfloor(start);
            System.out.println(result);
        }
    }

    public static int moveInWall () { // 시간의 벽에서 이동하는 함수
        int[][][] visited = new int[6][M][M];
        visited[machine[0]][machine[1]][machine[2]] = 1;

        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{machine[0], machine[1], machine[2]});

        while (!q.isEmpty()) {
            int[] cur = q.poll();

            if (cur[0] == BOTTOM) {
                return visited[BOTTOM][cur[1]][cur[2]] - 1;
            }

            for (int d = 0; d < 4; d++) {
                int nr = cur[1] + deltas[d][0];
                int nc = cur[2] + deltas[d][1];
                
                if (isIn(nr, nc, M)) { // 같은 면에서 이동
                    if (visited[cur[0]][nr][nc] == 0 && wall[cur[0]][nr][nc] == 0) {
                        visited[cur[0]][nr][nc] = visited[cur[0]][cur[1]][cur[2]] + 1;
                        q.offer(new int[]{cur[0], nr, nc});
                    }
                } else { // 다른 면으로 이동
                    int np = nextPos[cur[0]][d];

                    int[] next = calcNextPos(cur[0], np, cur[1], cur[2]);

                    if (visited[np][next[0]][next[1]] == 0) {
                        if ((np == BOTTOM && wall[BOTTOM][next[0]][next[1]] == -1) || (np != BOTTOM && wall[np][next[0]][next[1]] == 0)) {
                            visited[np][next[0]][next[1]] = visited[cur[0]][cur[1]][cur[2]] + 1;
                            q.offer(new int[]{np, next[0], next[1]});
                        }
                    }
                }
            }
        }

        return 0;
    }

    public static int[] calcNextPos (int CUR, int NEXT, int r, int c) { // 시간의 벽에서 다음 위치를 반환하는 함수
        if (NEXT == BOTTOM) {
            if (CUR == EAST) {
                return new int[] {M - 1 - c, r};
            } else if (CUR == WEST) {
                return new int[] {c, M - 1 - r};
            } else if (CUR == NORTH) {
                return new int[] {M - 1 - r, M - 1 - c};
            } else {
                return new int[] {r, c};
            }
        }

        if ((CUR == TOP && NEXT == EAST) || (CUR == EAST && NEXT == TOP)) {
            return new int[] {M - 1 - c, M - 1 - r};
        } else if ((CUR == TOP && NEXT == WEST) || (CUR == WEST && NEXT == TOP)) {
            return new int[] {c, r};
        } else if ((CUR == TOP && NEXT == SOUTH) || (CUR == SOUTH && NEXT == TOP)) {
            return new int[] {M - 1 - r, c};
        } else if ((CUR == TOP && NEXT == NORTH) || (CUR == NORTH && NEXT == TOP)) {
            return new int[] {r, M - 1 - c};
        } else {
            return new int[] {r, M - 1 - c};
        }
    }

    public static void spreadTime (int time) { // 시간의 이상 현상을 일으키는 함수
        int size = fires.size();

        while (size-- > 0) {
            Fire fire = fires.poll();

            if (time % fire.v == 0) {
                int nr = fire.r + deltas[fire.d][0];
                int nc = fire.c + deltas[fire.d][1];

                if (isIn(nr, nc, N) && floor[nr][nc] == 0) {
                    fire.r = nr;
                    fire.c = nc;

                    floor[nr][nc] = 1;

                    fires.offer(fire);
                }
            } else {
                fires.offer(fire);
            }
        }
    }

    public static int moveInfloor(int[] pos) { // 미지의 공간에서 이동하는 함수
        boolean[][] visited = new boolean[N][N];
        visited[pos[0]][pos[1]] = true;

        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{pos[0], pos[1]});

        while (!q.isEmpty()) {
            spreadTime(time);

            int size = q.size();

            while (size-- > 0) {
                int[] cur = q.poll();

                for (int d = 0; d < 4; d++) {
                    int nr = cur[0] + deltas[d][0];
                    int nc = cur[1] + deltas[d][1];

                    if (isIn(nr, nc, N) && !visited[nr][nc]) {
                        if (floor[nr][nc] == 0) {
                            visited[nr][nc] = true;
                            q.offer(new int[]{nr, nc});
                        } else if (floor[nr][nc] == 4) {
                            return time;
                        }
                    }
                }
            }

            time++;
        }

        return -1;
    }

    public static boolean isIn (int r, int c, int K) { // 현재 위치가 범위 내에 있는지 판단하는 함수
        return r >= 0 && r < K && c >= 0 && c < K;
    }
}