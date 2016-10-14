package net.byyd.archive.transform;

public interface Transformer<A,T> {
	
	/**
	 * Transforms an archive model of a certain version into the desired output model.
	 * 
	 * @param model
	 * @return
	 */
	T transform(A model);
}
