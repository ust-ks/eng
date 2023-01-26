package com.example.eng;

import java.util.List;

public class ListDir {
    public int[] id_dirs;
    public String[] names_dirs;
    public int[] NextRepetitions_dirs;

    public ListDir(List<String> List_dir, List<Integer> List_dir_id, List<Integer> List_dir_NR){
        this.names_dirs = new String[List_dir.size()];
        this.id_dirs = new int[List_dir.size()];
        this.NextRepetitions_dirs = new int[List_dir.size()];

        int i = 0;
        for (String element:
                List_dir) {
            names_dirs[i] = element;
            i+=1;
        }

        i=0;
        for (int element:
                List_dir_id) {
            id_dirs[i] = element;
            i+=1;
        }

        i=0;
        for (int element:
                List_dir_NR) {
            NextRepetitions_dirs[i] = element;
            i+=1;
        }
    }
}
