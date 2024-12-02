import java.util.*;
import java.io.*;

public class Main {
    static class Warrior {
        int r, c;
        boolean isRock, isAlive;

        public Warrior (int r, int c) {
            this.r = r;
            this.c = c;
            this.isRock = false;
            this.isAlive = true;
        }
    }

    public static int[][] deltas1 = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // 상, 하, 좌, 우
    public static int[][] deltas2 = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // 좌, 우, 상, 하

    public static int N;
    public static int[] medusa, park;
    public static int[][] count, town;
    public static List<Warrior> warriors;
    public static boolean[][] state, result;
    public static int maxCount;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        StringBuilder sb = new StringBuilder();

        N = Integer.parseInt(st.nextToken()); // 마을의 크기
        int M = Integer.parseInt(st.nextToken()); // 전사 수

        medusa = new int[2]; // 메두사 위치
        park = new int[2]; // 공원 위치(도착)
        
        st = new StringTokenizer(br.readLine());

        medusa[0] = Integer.parseInt(st.nextToken());
        medusa[1] = Integer.parseInt(st.nextToken());

        park[0] = Integer.parseInt(st.nextToken());
        park[1] = Integer.parseInt(st.nextToken());

        count = new int[N][N]; // 마을에서 각 칸마다 전사 수 카운팅

        warriors = new ArrayList(); // 전사 정보 리스트

        st = new StringTokenizer(br.readLine());

        for (int i = 0; i < M; i++) {
            int r = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());

            count[r][c]++;
            warriors.add(new Warrior(r, c));
        }

        town = new int[N][N]; // 마을 기본 정보(도로, 비도로 표시)
        
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());

            for (int j = 0; j < N; j++) {
                town[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        while (true) {
            // 1. 메두사 이동
            int val = moveMedusa(new int[] {medusa[0], medusa[1]});

            if (val != 1) {
                sb.append(val).append("\n");
                break;
            }

            for (Warrior warrior : warriors) {
                if (!warrior.isAlive) {
                    continue;
                }

                if (warrior.r == medusa[0] && warrior.c == medusa[1]) {
                    warrior.isAlive = false;
                    count[warrior.r][warrior.c]--;
                }
            }

            // 2. 메두사의 시선
            maxCount = 0;
            result = new boolean[N][N];

            for (int d = 0; d < 4; d++) {
                countWarriors(d);

                int cnt = 0;

                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        if (state[i][j] && count[i][j] > 0) {
                            cnt += count[i][j];
                        }
                    }
                }

                if (cnt > maxCount) {
                    maxCount = cnt;

                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < N; j++) {
                            result[i][j] = state[i][j];
                        }
                    }
                }
            }

            result[medusa[0]][medusa[1]] = false;

            for (Warrior warrior : warriors) {
                if (!warrior.isAlive) {
                    continue;
                }

                if (result[warrior.r][warrior.c]) {
                    warrior.isRock = true;
                }
            }

            // 3. 전사들의 이동
            int totalDistance = 0;

            for (Warrior warrior : warriors) {
                if (!warrior.isAlive || warrior.isRock) {
                    continue;
                }

                int cnt = moveWarrior(warrior, deltas1);

                if (cnt > 0) {
                    totalDistance += cnt + moveWarrior(warrior, deltas2);
                }
            }

            // 4. 전사의 공격
            int attacker = 0;

            for (Warrior warrior : warriors) {
                if (!warrior.isAlive || warrior.isRock) {
                    continue;
                }

                if (warrior.r == medusa[0] && warrior.c == medusa[1]) {
                    count[warrior.r][warrior.c]--;
                    attacker++;
                    warrior.isAlive = false;
                }
            }

            // 5. 돌로 변한 전사들 다시 돌려놓기
            for (Warrior warrior : warriors) {
                if (!warrior.isAlive) {
                    continue;
                }

                if (warrior.isRock) {
                    warrior.isRock = false;
                }
            }

            sb.append(totalDistance).append(" ").append(maxCount).append(" ").append(attacker).append("\n");
        }

        System.out.println(sb);
    }

    public static int moveMedusa (int[] start) { // 메두사를 이동하는 함수
        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{medusa[0], medusa[1]});

        int[][] visited = new int[N][N];

        for (int i = 0; i < N; i++) {
            Arrays.fill(visited[i], -1);
        }

        visited[medusa[0]][medusa[1]] = -2;

        while (!q.isEmpty()) {
            int[] cur = q.poll();

            if (cur[0] == park[0] && cur[1] == park[1]) {
                break;
            }

            for (int d = 0; d < 4; d++) {
                int nr = cur[0] + deltas1[d][0];
                int nc = cur[1] + deltas1[d][1];

                if (isIn(nr, nc) && town[nr][nc] == 0 && visited[nr][nc] == -1) {
                    q.offer(new int[]{nr, nc});
                    visited[nr][nc] = d;
                }
            }
        }

        int r = park[0];
        int c = park[1];

        if (visited[r][c] == -1) {
            return -1;
        }

        while (true) {
            int d = visited[r][c] + 1;

            if (d % 2 == 0) {
                d -= 2;
            }

            int nr = r + deltas1[d][0];
            int nc = c + deltas1[d][1];

            if (visited[nr][nc] == -2) {
                medusa[0] = r;
                medusa[1] = c;
                break;
            }

            r = nr;
            c = nc;
        }

        if (medusa[0] == park[0] && medusa[1] == park[1]) {
            return 0;
        }

        return 1;
    }

    public static void countWarriors (int dir) { // 메두사의 시선 방향에 따라 돌로 만들 수 있는 전사들을 세는 함수
        state = new boolean[N][N];
        state[medusa[0]][medusa[1]] = true;

        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{medusa[0], medusa[1]});

        while (!q.isEmpty()) {
            int[] cur = q.poll();

            for (int i = -1; i <= 1; i++) {
                int nr, nc;

                if (dir / 2 == 0) { // 상, 하
                    nr = cur[0] + deltas1[dir][0];
                    nc = cur[1] + i;
                } else { // 좌, 우
                    nr = cur[0] + i;
                    nc = cur[1] + deltas1[dir][1];
                }

                if (isIn(nr, nc) && !state[nr][nc]) {
                    q.offer(new int[]{nr, nc});
                    state[nr][nc] = true;
                }
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (state[i][j] && count[i][j] > 0) {
                    int[] deltas;

                    if (dir / 2 == 0) { // 상, 하
                        if (j < medusa[1]) {
                            deltas = new int[] {-1, 0};
                        } else if (j == medusa[1]) {
                            deltas = new int[]{0};
                        } else {
                            deltas = new int[]{0, 1};
                        }
                    } else { // 좌, 우
                        if (i < medusa[0]) {
                            deltas = new int[] {-1, 0};
                        } else if (i == medusa[0]) {
                            deltas = new int[]{0};
                        } else {
                            deltas = new int[]{0, 1};
                        }
                    }

                    hide(dir, new int[]{i, j}, deltas);
                }
            }
        }
    }

    public static void hide (int dir, int[] pos, int[] deltas) {
        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{pos[0], pos[1]});

        state[pos[0]][pos[1]] = true;

        while (!q.isEmpty()) {
            int[] cur = q.poll();

            int nr, nc;

            for (int d : deltas) {
                if (dir / 2 == 0) { // 상, 하
                    nr = cur[0] + deltas1[dir][0];
                    nc = cur[1] + d;
                } else { // 좌, 우
                    nr = cur[0] + d;
                    nc = cur[1] + deltas1[dir][1];
                }

                if (isIn(nr, nc) && state[nr][nc] && !(nr == pos[0] && nc == pos[1])) {
                    q.offer(new int[]{nr, nc});
                    state[nr][nc] = false;
                }
            }
        }
    }
    
    public static int moveWarrior (Warrior warrior, int[][] deltas) { // 전사를 이동시키는 함수
        int nowDistance = Math.abs(warrior.r - medusa[0]) + Math.abs(warrior.c - medusa[1]);
        int dir = -1;

        for (int d = 0; d < 4; d++) {
            int nr = warrior.r + deltas[d][0];
            int nc = warrior.c + deltas[d][1];

            if (!isIn(nr, nc) || result[nr][nc]) {
                continue;
            }

            int dist = Math.abs(nr - medusa[0]) + Math.abs(nc - medusa[1]);

            if (dist < nowDistance) {
                dir = d;
                nowDistance = dist;
            }
        }

        if (dir == -1) {
            return 0;
        }

        count[warrior.r][warrior.c]--;

        warrior.r += deltas[dir][0];
        warrior.c += deltas[dir][1];

        count[warrior.r][warrior.c]++;

        return 1;
    }

    public static boolean isIn (int r, int c) { // 현재 좌표가 공원 내에 있는지 판별하는 함수
        return r >= 0 && r < N && c >= 0 && c < N;
    }
}