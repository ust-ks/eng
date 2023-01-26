package com.example.eng;

import java.util.List;

public class ListWords {
    public String[] str;
    public String[] words;
    public String[] translations;
    public int[] id_words;

    public ListWords(List<String> List_word, List<String> List_translation, List<Integer> List_id){
        this.str = new String[List_word.size()];
        this.words = new String[List_word.size()];
        this.translations = new String[List_word.size()];
        this.id_words = new int[List_word.size()];

        int i = 0;
        for (String element:
                List_word) {
            words[i] = element;
            str[i] = element;
            i+=1;
        }

        i=0;
        for (String element:
                List_translation) {
            translations[i] = element;
            str[i] += " - " + element;
            i+=1;
        }

        i=0;
        for (int element:
                List_id) {
            id_words[i] = element;
            i+=1;
        }
    }
}
