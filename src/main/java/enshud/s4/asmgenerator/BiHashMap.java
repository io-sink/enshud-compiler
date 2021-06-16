package enshud.s4.asmgenerator;

import java.util.HashMap;
import java.util.Set;

public class BiHashMap<K, V> {
	public HashMap<K, V> map;
	public HashMap<V, K> mapInv;

	public BiHashMap() {
		map = new HashMap<K, V>();
		mapInv = new HashMap<V, K>();
	}

	public V get(K key) {
		return map.get(key);
	}

	public K getInv(V key) {
		return mapInv.get(key);
	}

	public V put(K key, V value) {
		V res = get(key);
		if(map.containsKey(key))
			mapInv.remove(map.get(key));
		if(mapInv.containsKey(value))
			map.remove(mapInv.get(value));

		map.put(key, value);
		mapInv.put(value, key);
		return res;
	}

	public K putInv(V key, K value) {
		K res = getInv(key);
		if(mapInv.containsKey(key))
			map.remove(mapInv.get(key));
		if(map.containsKey(value))
			mapInv.remove(map.get(value));

		mapInv.put(key, value);
		map.put(value, key);
		return res;
	}

	public V remove(K key) {
		V value = map.remove(key);
		if(value != null)
			mapInv.remove(value);
		return value;
	}

	public K removeInv(V key) {
		K value = mapInv.remove(key);
		if(value != null)
			map.remove(value);
		return value;
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public Set<V> keySetInv() {
		return mapInv.keySet();
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public boolean containsKeyInv(V key) {
		return mapInv.containsKey(key);
	}
}