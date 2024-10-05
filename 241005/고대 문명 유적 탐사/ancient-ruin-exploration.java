import java.util.*;
import java.io.*;

public class Main {
    public static StringBuilder sb;
    public static int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    public static int[] nums;
    public static int idx;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        sb = new StringBuilder();

        int K = Integer.parseInt(st.nextToken());
        int M = Integer.parseInt(st.nextToken());

        int[][] map = new int[5][5];
    
        for (int i = 0; i < 5; i++) {
            st = new StringTokenizer(br.readLine());

            for (int j = 0; j < 5; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        nums = new int[M];
        idx = 0;

        st = new StringTokenizer(br.readLine());

        for (int i = 0; i < M; i++) {
            nums[i] = Integer.parseInt(st.nextToken());
        }

        for (int k = 1; k <= K; k++) {
            int answer = 0;

            // 1. 탐사 진행
            int maxVal = 0;
            int[] pos = new int[2];
            int turnCnt = 0;

            for (int i = 3; i >= 1; i--) {
                for (int j = 3; j >= 1; j--) {
                    int[][] tempMap = new int[5][5];

                    copy(tempMap, map);

                    // 90, 180, 270 회전
                    for (int t = 1; t <= 3; t++) {
                        turn(tempMap, new int[]{i, j});

                        int cnt = check(tempMap);

                        if (maxVal < cnt || (maxVal == cnt && turnCnt <= t)) {
                            maxVal = cnt;
                            pos[0] = i;
                            pos[1] = j;
                            turnCnt = t;
                        }
                    }
                }
            }

            int[][] tempMap = new int[5][5];

            copy(tempMap, map);

            // 최종 회전
            for (int i = 1; i <= turnCnt; i++) {
                turn (tempMap, new int[]{pos[0], pos[1]});
            }

            copy(map, tempMap);

            answer += maxVal;

            // 2. 유물 획득
            get(map);

            while (true) {
                int val = check(map);

                if (val == 0) {
                    break;
                }

                answer += val;
                get(map);
            }

            if (answer > 0) {
                sb.append(answer).append(" ");
            }
        }

        System.out.println(sb);
    }

    public static void turn (int[][] oldMap, int[] center) {
        int[][] newMap = new int[5][5];

        copy(newMap, oldMap);

        for (int i = -1; i <= 1; i++) {
            for (int j =-1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                int r = center[0] + i;
                int c = center[1] + j;

                newMap[r][c] = oldMap[c + center[0] - center[1]][center[0] + center[1] - r];
            }
        }

        copy(oldMap, newMap);
    }

    // 획득 가능한 유물 수 찾기
    public static int find(int[] start, int val, boolean[][] visited, int[][] map) {
        visited[start[0]][start[1]] = true;

        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{start[0], start[1]});

        int count = 1;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            
            for (int d = 0; d < 4; d++) {
                int nr = cur[0] + deltas[d][0];
                int nc = cur[1] + deltas[d][1];

                if (isIn(nr, nc) && !visited[nr][nc] && map[nr][nc] == val) {
                    q.offer(new int[]{nr, nc});
                    visited[nr][nc] = true;
                    count++;
                }
            }

        }

        if (count >= 3) {
            return count;
        }

        return 0;
    }

    // 획득한 유물 수 자리 비우기
    public static void remove (int[] start, int val, boolean[][] visited, int[][] map) {
        visited[start[0]][start[1]] = true;

        Queue<int[]> q = new ArrayDeque();
        q.offer(new int[]{start[0], start[1]});

        List<int[]> list = new ArrayList();
        list.add(new int[]{start[0], start[1]});

        while (!q.isEmpty()) {
            int[] cur = q.poll();

            for (int d = 0; d < 4; d++) {
                int nr = cur[0] + deltas[d][0];
                int nc = cur[1] + deltas[d][1];

                if (isIn(nr, nc) && !visited[nr][nc] && map[nr][nc] == val) {
                    visited[nr][nc] = true;
                    q.offer(new int[]{nr, nc});
                    list.add(new int[]{nr, nc});
                }  
            }
        }

        if (list.size() >= 3) {
            for (int[] p : list) {
                map[p[0]][p[1]] = 0;
            }
        }
    }

    // 새로운 조각으로 채우기
    public static void fill (int[][] map) {
        for (int j = 0; j < 5; j++) {
            for (int i = 4; i >= 0; i--) {
                if (map[i][j] == 0) {
                    map[i][j] = nums[idx++];
                }
            }
        }
    }

    // 획득 가능한 총 유물 수 확인
    public static int check (int[][] map) {
        boolean[][] visited = new boolean[5][5];

        int cnt = 0;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!visited[i][j]) {
                    cnt += find(new int[]{i, j}, map[i][j], visited, map);
                }
            }
        }

        return cnt;
    }

    // 획득한 유물 자리 지우고, 새로운 조각으로 채우기
    public static void get (int[][] map) {
        boolean[][] visited = new boolean[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!visited[i][j]) {
                    remove(new int[]{i, j}, map[i][j], visited, map);
                }
            }
        }

        fill(map);
    }

    // 배열 복사
    public static void copy (int[][] fresh, int[][] origin) {
        for (int i = 0; i < 5; i++) {
            fresh[i] = origin[i].clone();
        }
    }

    // 범위 확인
    public static boolean isIn (int r, int c) {
        return r >= 0 && r < 5 && c >= 0 && c < 5;
    }
}