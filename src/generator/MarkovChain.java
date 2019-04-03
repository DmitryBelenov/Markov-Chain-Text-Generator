package generator;

import java.util.*;

/**
 * Генератор текстов на основе цепей Маркова
 *
 * author Belenov D.
 * */

public class MarkovChain {
    // исходный корпус (желательно предварительно пропустить через онлайн-форматтер)
    public static final String text = "text for generate here (you can try \"example.txt\" content)";

    // очистка и подготовка текста
    public static String clean (String txt) {
        String clean = txt.replace("\n","")
                .replaceAll("[?!]",".")
                .replaceAll("[()\\]:\"\"A-Za-z]","");
        StringBuilder sb = new StringBuilder();
        String [] s = clean.split(" ");

        for (String word : s) {
            sb.append(word.trim()+" ");
        }
        return sb.toString();
    }

    // класс для промежуточной обработки словесных пар - биграмм
    public static class Pairs
    {
        private String key;
        private String value;

        public Pairs(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public String getKey()   { return key; }
        public String getValue() { return value; }
    }

    // подготовка биграмм с назначением начала / конца предложения
    public static List prepare (String text) {
        String [] ss = text.split("\\. ");
        List<List<String>> prePost = new ArrayList<>();

        for (int i=0; i<ss.length; i++) {
            String [] sents = ss[i].split(" ");
            int sz = sents.length;
            sents[0] = "*s* "+sents[0];
            sents[sz-1] += " *e*";

            prePost.add(Arrays.asList(sents));
        }

        StringBuilder sb = new StringBuilder();
        prePost.stream().forEach(p->{
            p.stream().forEach(word->{
                sb.append(word+" ");
            });
        });

        List<String> listOfPairs = new LinkedList<>();
        String [] arr = sb.toString().split(" ");
        int i = 1;
        for (String word : arr) {
            if (i<arr.length) {
                Pairs pair = new Pairs(word, arr[i]);
                listOfPairs.add(pair.getKey()+"->"+pair.getValue());
                i++;
            } else {
                break;
            }
        }

        List<List<String>> baseList = new ArrayList<>();

        listOfPairs.stream().forEach(pair->{
            String [] pArr = pair.split("->");
            List<String> kv = Arrays.asList(pArr);
            baseList.add(kv);
        });

        return baseList;
    }

    // сортировка готовой коллекции биграмм на ключи, значения и их кол-ва (параметр - метод prepare)
    public static Map sorting (List<List<String>> baseList) {
        Map<String, Map<String, Integer>> map = new HashMap<>();

        baseList.forEach(pair-> {
            String key = pair.get(0);
            String value = pair.get(1);

            if (map.size() < 1) {
                Map<String, Integer> valueIndex = new HashMap<>();
                valueIndex.put(value, 1);
                map.put(key, valueIndex);
            } else {
                try {
                    for (String k : map.keySet()) {
                        if (k.equals(key)) {
                            Map<String, Integer> valueIndex = map.get(key);
                            for (String kv : valueIndex.keySet()) {
                                if (kv.equals(value))
                                    valueIndex.put(value, valueIndex.get(value)+1);
                                else
                                    valueIndex.put(value, 1);
                            }
                            map.put(key, valueIndex);
                            break;
                        } else {
                            Map<String, Integer> valueIndex;
                            if (map.keySet().contains(key)) {
                                valueIndex = map.get(key);
                                for (String kv : valueIndex.keySet()) {
                                    if (kv.equals(value)) {
                                        valueIndex.put(value, valueIndex.get(value) + 1);
                                        break;
                                    } else {
                                        valueIndex.put(value, 1);
                                    }
                                }
                            } else {
                                valueIndex = new HashMap<>();
                                valueIndex.put(value, 1);
                            }
                            map.put(key, valueIndex);
                            break;
                        }
                    }
                  // отлавливаем исключение преобразования коллекции во время итерации по ней
                } catch(ConcurrentModificationException e){}
            }
        });
        return map;
    }

    // генерация текста из готового маппинга ключей/значений+кол-ва (метод sorting)
    public static String generate (Map<String, Map<String, Integer>> baseMap) {
        List<String> baseList = new LinkedList<>();

        List<String> keys = new ArrayList<>();
        baseMap.keySet().forEach(key->{
            keys.add(key);
        });
        baseList.add("*s*");

        int s = 0;
        while (s<baseMap.size()) {   // размер сгенерированного текста = размеру исходного
            String last = baseList.get(baseList.size() - 1);
            Map<String, Integer> value = baseMap.get(last);

            String val;
            List<String> valueKeys = new ArrayList<>();
            value.keySet().forEach(vks -> {
                valueKeys.add(vks);
            });

            if (valueKeys.contains("*e*")) {
                val="*e*";
            } else {
                Random r = new Random();
                int i = valueKeys.size() - 1;
                int random = r.nextInt((i - 0) + 1) + 0;
                val = valueKeys.get(random); // слово берется рандомно, без учета его частоты
            }
            baseList.add(val);
            s++;
        }

        StringBuilder ready = new StringBuilder();

        baseList.forEach(index->{
            if (index.equals("*s*")) ready.append("");
            else
            if (index.equals("*e*")) ready.append(". ");
            else ready.append(index+" ");
        });

        return ready.toString();
    }

    // метод main для запуска генерации (выводит результат в консоль)
    public static void main(String[] args) {
        System.out.println(generate(sorting(prepare(clean(text)))));
    }
}