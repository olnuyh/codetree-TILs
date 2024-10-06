import java.io.*;
import java.util.*;

public class Main {
    public static int[][] deltas = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; // 북, 동, 남, 서
    public static int[][] forest;
    public static int R, C;
    public static int answer;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken());

        forest = new int[R][C];

        answer = 0;

        for (int k = 1; k <= K; k++) {
            st = new StringTokenizer(br.readLine());

            int c = Integer.parseInt(st.nextToken()) - 1;
            int dir = Integer.parseInt(st.nextToken()); // 출구 위치

            // 1. 골렘 이동

            int[] center = {-2, c};

            while (canGo(center[0] + 1, center[1], 0)) { // 남쪽 이동
                center[0]++;
            }

            while (canGo(center[0], center[1] - 1, 1) && canGo(center[0] + 1, center[1] - 1, 1)) { // 서쪽 이동
                center[0]++;
                center[1]--;
                dir = (dir + 3) % 4;
            }

            while (canGo(center[0], center[1] + 1, 3) && canGo(center[0] + 1, center[1] + 1, 3)) { // 동쪽 이동
                center[0]++;
                center[1]++;
                dir = (dir + 1) % 4;
            }

            if (center[0] < 0) { // 숲을 벗어난 경우 초기화
                for (int i = 0; i < R; i++) {
                    Arrays.fill(forest[i], 0);
                }

                continue;
            }

            arrive(center[0], center[1], dir, k); // 최종 도착 위치 저장

            answer += move(center[0], center[1], k); // 최종 정령 위치 저장
        }

        System.out.println(answer);
    }

    // 이동이 가능한지 확인하는 함수
    public static boolean canGo (int r, int c, int dir) { // 기준 위치, 확인하지 않아도 되는 방향
        for (int d = 0; d < 4; d++) {
            if (d == dir) {
                continue;
            }

            int nr = r + deltas[d][0];
            int nc = c + deltas[d][1];

            if (!isIn(nr, nc)) {
                return false;
            }

            if (nr >= 0 && forest[nr][nc] != 0) {
                return false;
            }
        }

        return true;
    }

    // 최종 도착 위치를 저장하는 함수
    public static void arrive (int r, int c, int dir, int k) {
        forest[r][c] = k;

        for (int d = 0; d < 4; d++) {
            int nr = r + deltas[d][0];
            int nc = c + deltas[d][1];

            if (d == dir) {
                forest[nr][nc] = -k;
            } else {
                forest[nr][nc] = k;
            }
        }
    }

    // 정령이 최종적으로 위치한 행을 구하는 함수
    public static int move (int r, int c, int k) {
        boolean[][] visited = new boolean[R][C];
        visited[r][c] = true;

        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{r, c, k});

        int maxR = 0;

        while (!q.isEmpty()) {
            int[] cur = q.poll();

            maxR = Math.max(maxR, cur[0]);

            for (int d = 0; d < 4; d++) {
                int nr = cur[0] + deltas[d][0];
                int nc = cur[1] + deltas[d][1];
                
                if (nr < 0 || !isIn(nr, nc) || visited[nr][nc]) {
                    continue;
                }

                int val = forest[nr][nc];

                if ((cur[2] < 0 && val != 0) || cur[2] == val || val == -cur[2]) {
                    visited[nr][nc] = true;
                    q.offer(new int[]{nr, nc, val});
                } 
            }
        }

        return maxR + 1;
    }

    // 범위 확인하는 함수
    public static boolean isIn (int r, int c) {
        return r < R && c >= 0 && c < C;
    }
}