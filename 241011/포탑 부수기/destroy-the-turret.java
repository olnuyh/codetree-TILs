import java.util.*;
import java.io.*;

public class Main {
    static class Turret {
        int r, c, power, attackTime;

        public Turret (int r, int c, int power) {
            this.r = r;
            this.c = c;
            this.power = power;
            attackTime = 0;
        }
    }

    public static int N, M;
    public static int count;
    public static Turret[][] turrets;
    public static int[][] deltas = {{0, 1}, {1, 0 }, {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    public static int[][] visited;
    public static boolean[][] check;
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        N = Integer.parseInt(st.nextToken()); // 행
        M = Integer.parseInt(st.nextToken()); // 열
        int K = Integer.parseInt(st.nextToken()); // 턴 횟수

        turrets = new Turret[N][M];
        count = 0;

        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());

            for (int j = 0; j < M; j++) {
                turrets[i][j] = new Turret(i, j, Integer.parseInt(st.nextToken()));
                if (turrets[i][j].power > 0) {
                    count++;
                }
            }
        }

        for (int t = 1; t <= K; t++) {
            // 1. 공격자 선정
            Turret attacker = chooseAttacker();

            // 2. 공격 대상자 선정
            Turret target = chooseTarget();

            attacker.power += N + M;
            attacker.attackTime = t;

            // 3. 레이저 공격 시도
            visited = new int[N][M];
            check = new boolean[N][M];

            if (tryLaserAttack(new int[]{attacker.r, attacker.c}, new int[]{target.r, target.c})) {
                laserAttack(new int[]{target.r, target.c}, new int[]{attacker.r, attacker.c}, attacker.power / 2);
                
                turrets[target.r][target.c].power -= attacker.power / 2;
                
                if (attacker.power % 2 == 1) {
                    turrets[target.r][target.c].power--;
                }
            } else { // 레이저 공격 불가 시 포탄 공격
                check[target.r][target.c] = true;
                turrets[target.r][target.c].power -= attacker.power;
                bombAttack(new int[]{target.r, target.c}, attacker.power / 2);
            }

            check[attacker.r][attacker.c] = true;

            // 4. 포탑 부서짐 확인 및 포탑 정비
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    if (check[i][j] && turrets[i][j].power <= 0) { // 공격 받은 포탑 부서짐 확인 
                        count--;
                        turrets[i][j].power = 0;
                    } else if (!check[i][j] && turrets[i][j].power != 0) { // 공격과 무관한 포탑 정비
                        turrets[i][j].power++;
                    }
                }
            }

            if (count == 1) {
                break;
            }
        }

        int maxPower = 0;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                maxPower = Math.max(maxPower, turrets[i][j].power);
            }
        }

        System.out.println(maxPower);
    }

    // 공격자를 선정하는 함수
    public static Turret chooseAttacker () {
        Turret attacker = new Turret(-1, -1, Integer.MAX_VALUE);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (turrets[i][j].power == 0 || turrets[i][j].power > attacker.power) {
                    continue;
                }

                if (turrets[i][j].power == attacker.power) {
                    if (turrets[i][j].attackTime < attacker.attackTime) {
                        continue;
                    }

                    if (turrets[i][j].attackTime == attacker.attackTime) {
                        if (turrets[i][j].r + turrets[i][j].c < attacker.r + attacker.c) {
                            continue;
                        }

                        if (turrets[i][j].r + turrets[i][j].c == attacker.r + attacker.c) {
                            if (turrets[i][j].c < attacker.c) {
                                continue;
                            }
                        }
                    }
                }

                attacker = turrets[i][j];
            }
        }

        return attacker;
    }

    // 공격 대상자를 선정하는 함수
    public static Turret chooseTarget () {
        Turret target = new Turret(N, M, Integer.MIN_VALUE);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (turrets[i][j].power == 0 || turrets[i][j].power < target.power) {
                    continue;
                }

                if (turrets[i][j].power == target.power) {
                    if (turrets[i][j].attackTime > target.attackTime) {
                        continue;
                    }

                    if (turrets[i][j].attackTime == target.attackTime) {
                        if (turrets[i][j].r + turrets[i][j].c > target.r + target.c) {
                            continue;
                        }

                        if (turrets[i][j].r + turrets[i][j].c == target.r + target.c) {
                            if (turrets[i][j].c > target.c) {
                                continue;
                            }
                        }
                    }
                }

                target = turrets[i][j];
            }
        }

        return target;
    }

    // 레이저 공격을 시도하는 함수
    public static boolean tryLaserAttack (int[] start, int[] end) {
        Queue<int[]> q = new ArrayDeque();
        q.offer(start);

        int cnt = 1;
        visited[start[0]][start[1]] = cnt++;

        while (!q.isEmpty()) {
            int size = q.size();

            while (size-- > 0) {
                int[] cur = q.poll();

                if (cur[0] == end[0] && cur[1] == end[1]) {
                    return true;
                }

                for (int d = 0; d < 4; d++) {
                    int nr = (cur[0] + deltas[d][0] + N) % N;
                    int nc = (cur[1] + deltas[d][1] + M) % M;

                    if (visited[nr][nc] == 0 && turrets[nr][nc].power != 0) {
                        visited[nr][nc] = cnt;
                        q.offer(new int[]{nr, nc});
                    }
                }
            }

            cnt++;
        }

        return false;
    }

    // 레이저 공격 결과를 적용하는 함수
    public static boolean laserAttack(int[] cur, int[] start, int power) {
        if (cur[0] == start[0] && cur[1] == start[1]) {
            return true;
        }

        check[cur[0]][cur[1]] = true;
        turrets[cur[0]][cur[1]].power -= power;

        for (int d = 3; d >= 0; d--) {
            int nr = (cur[0] + deltas[d][0] + N) % N;
            int nc = (cur[1] + deltas[d][1] + M) % M;

            if (visited[nr][nc] == visited[cur[0]][cur[1]] - 1) {
                if (laserAttack(new int[]{nr, nc}, start, power)) {
                    break;
                }
            }
        }

        return true;
    }

    // 포탄 공격하는 함수
    public static void bombAttack (int[] target, int power) {
        for (int d = 0; d < 8; d++) {
            int nr = (target[0] + deltas[d][0] + N) % N;
            int nc = (target[1] + deltas[d][1] + M) % M;

            if (turrets[nr][nc].power == 0) {
                continue;
            }

            turrets[nr][nc].power -= power;
            check[nr][nc] = true;
        }
    }
}