package uf.morpheus.meta;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import uf.morpheus.meta.Constants.StringDistanceAlgorithm;

import fr.inrialpes.exmo.align.impl.method.StringDistances;

/**
 * @author clint
 *
 */
public class Utils 
{
	/**
	 * Generates the log message
	 * 
	 * @param message
	 */
	public static void showLog(String message)
	{
		System.out.println(getTime() + " " + message);
	}
	
	public static String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());

	}
	
	/**
	 * Matches two given URIs based on some matching algorithm...
	 * 
	 * @param uri - OWL URI 1 
	 * @param uri2 - OWL URI 2
	 * */
	public static double findURISimilarity(URI uri, URI uri2, StringDistanceAlgorithm stringMatchingAlgorithm) 
	{
		double dist = Constants.DISSIMILARITY; 
		
		if (stringMatchingAlgorithm == StringDistanceAlgorithm.LEVENSHTEIN_DISTANCE)
			dist = StringDistances.levenshteinDistance(uri.getFragment(), uri2.getFragment());
		else if (stringMatchingAlgorithm == StringDistanceAlgorithm.SUBSTRING_DISTANCE)
			dist = StringDistances.subStringDistance(uri.getFragment(), uri2.getFragment());
		else if (stringMatchingAlgorithm == StringDistanceAlgorithm.EQUAL_DISTANCE)
			dist = StringDistances.equalDistance(uri.getFragment(), uri2.getFragment());
		else if (stringMatchingAlgorithm == StringDistanceAlgorithm.JARO_MEASURE)
			dist = StringDistances.jaroMeasure(uri.getFragment(), uri2.getFragment());
		else if (stringMatchingAlgorithm == StringDistanceAlgorithm.JARO_WINKLER_MEASURE)
			dist = StringDistances.jaroWinklerMeasure(uri.getFragment(), uri2.getFragment());
		else if (stringMatchingAlgorithm == StringDistanceAlgorithm.NEEDLEMAN_WUNCH_2_DISTANCE)
			dist = StringDistances.needlemanWunch2Distance(uri.getFragment(), uri2.getFragment());
		else if (stringMatchingAlgorithm == StringDistanceAlgorithm.NGRAM_DISTANCE)
			dist = StringDistances.ngramDistance(uri.getFragment(), uri2.getFragment());
		else if (stringMatchingAlgorithm == StringDistanceAlgorithm.SMOA_DISTANCE)
			dist = StringDistances.smoaDistance(uri.getFragment(), uri2.getFragment());
		
		return dist;
	}
}
