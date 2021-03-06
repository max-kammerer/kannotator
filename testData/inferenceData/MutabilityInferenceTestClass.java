package inferenceData;

import inferenceData.annotations.ExpectMutable;

import java.util.*;

public class MutabilityInferenceTestClass {

    public void testMutableCollection(@ExpectMutable Collection<Integer> collection) {
        collection.add(2);
    }

    public void testImmutableCollection(Collection<Integer> collection) {
        collection.iterator();
    }

    public void testIterateOverMutableCollection(@ExpectMutable Collection<Integer> collection) {
        Iterator<Integer> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == 0) {
                iterator.remove();
            }
        }
    }

    public void testMapEntry(@ExpectMutable Map.Entry<Integer, String> entry) {
        entry.setValue("");
    }

    public void testChangeKeySetInMap(@ExpectMutable Map<Integer, String> map) {
        Set<Integer> set = map.keySet();
        set.remove(42);
    }

    public void testEntrySetInMap(@ExpectMutable Map<Integer, String> map) {
        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
        iterator.remove();
    }

    public void testEntrySetInMap2(@ExpectMutable Map<Integer, String> map) {
        Set<Map.Entry<Integer, String>> entrySet = map.entrySet();
        Iterator<Map.Entry<Integer, String>> iterator = entrySet.iterator();
        iterator.remove();
    }

    public void testInvokeProcessMutable(@ExpectMutable List<String> list) {
        new MutabilityInferenceTestLib().processMutable(list);
    }

    public void testInvokeProcessReadableAndMutable(@ExpectMutable Collection<String> collection, List<String> list) {
        MutabilityInferenceTestLib.processReadableAndMutable(list, collection);
    }
}
