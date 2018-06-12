package de.foellix.aql.helper;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BiMap<K, V> implements Serializable {
	private static final long serialVersionUID = -3705792113607914598L;

	Map<K, V> map = new HashMap<>();
	Map<V, K> inversedMap = new HashMap<>();

	public BiMap() {
		this.map = new HashMap<>();
		this.inversedMap = new HashMap<>();
	}

	public BiMap(final Map<K, V> data) {
		this.map = new HashMap<>();
		this.inversedMap = new HashMap<>();

		for (final K key : data.keySet()) {
			put(key, data.get(key));
		}
	}

	public void put(final K k, final V v) {
		this.map.put(k, v);
		this.inversedMap.put(v, k);
	}

	public V get(final K k) {
		return this.map.get(k);
	}

	public K getKey(final V v) {
		return this.inversedMap.get(v);
	}

	public Collection<V> values() {
		return this.map.values();
	}

	public Set<K> keySet() {
		return this.map.keySet();
	}

	public V remove(final Object key) {
		this.inversedMap.remove(this.map.get(key));
		return this.map.remove(key);
	}
}