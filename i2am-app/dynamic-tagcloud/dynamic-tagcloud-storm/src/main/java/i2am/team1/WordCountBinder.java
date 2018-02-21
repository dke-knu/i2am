package i2am.team1;

import java.util.ArrayList;
import java.util.List;

import org.apache.storm.trident.operation.ReducerAggregator;
import org.apache.storm.trident.tuple.TridentTuple;

public class WordCountBinder implements ReducerAggregator<List<WordCountElement>>{

	@Override
	public List<WordCountElement> init() {
		// TODO Auto-generated method stub
		return new ArrayList<WordCountElement>();
	}

	@Override
	public List<WordCountElement> reduce(List<WordCountElement> curr, TridentTuple tuple) {
		// TODO Auto-generated method stub
		curr.add(new WordCountElement(tuple.getString(0), tuple.getLong(1), tuple.getString(2)));
		return curr;
	}
}
