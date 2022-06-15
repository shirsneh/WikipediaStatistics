// package cloud.wikipedia;
//
// import cloud.wikipedia.model.WikiObject;
//
// import java.util.*;
// import java.util.stream.Collectors;
//
// public class MostActiveBla<K extends WikiObject> {
//    private final HashMap<String, K> mostActive = new HashMap<>();
//
//    public MostActiveBla<K> add(final K enriched) {
//        if (mostActive.containsKey(enriched.name)) {
//            int total = mostActive.get(enriched.name).counter + enriched.counter;
//            enriched.setCounter(total);
//            mostActive.put(enriched.name, enriched);
//        } else {
//            mostActive.put(enriched.name, enriched);
//        }
//
//        return this;
//    }
//
//    public List<K> toList() {
//        //    Iterator<K> objects = mostActive.iterator();
//        //    List<K> objectScores = new ArrayList<>();
//        //
//        //    while (objects.hasNext()) {
//        //      objectScores.add(objects.next());
//        //    }
//        //
//        //    return objectScores;
//        List<K> elements = new ArrayList<>(mostActive.values());
//        elements.sort((record1, record2) -> Integer.compare(record2.counter, record1.counter));
//
//        //        return elements;
//        return elements.stream().limit(Utils.MOST_ACTIVE_LIMIT).collect(Collectors.toList());
//    }
// }
