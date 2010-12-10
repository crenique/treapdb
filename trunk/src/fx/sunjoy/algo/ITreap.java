package fx.sunjoy.algo;

import java.util.Map;

public interface ITreap<K extends Comparable<K>, V> {

	//д��
	public abstract void put(K key, V value);

	//����
	public abstract V get(K key);

	//��Χ��ѯ
	public abstract Map<K,V> range(K start, K end,int limit);

	//����
	public abstract int length();

	//ɾ��
	public abstract boolean delete(K key);

	//ǰ׺
	public abstract Map<K,V> prefix(K prefixString,int limit);
	
	//<=key��
	public abstract Map<K,V> before(K key,int limit);
	
	//>=key��
	public abstract Map<K,V> after(K key,int limit);
	
	public Map<K,V> kmin(int k);
	
	public Map<K,V> kmax(int k);

}