package de.uni_hannover.inma.controller;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import android.os.AsyncTask;
import android.widget.Toast;
import de.uni_hannover.inma.R;
import de.uni_hannover.inma.model.MainActivity;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class InformUsTask extends AsyncTask<Way, Integer, Integer> {

	private MainActivity v;
	private Coordinate location;
	
	public InformUsTask(MainActivity v, Coordinate l) {
		this.v = v;
		this.location = l;
	}
	
	@Override
	protected Integer doInBackground(Way... params) {
		for(Way w: params) {
			if(w==null)
				continue;
			Connection con = Jsoup.connect("http://www.sur.gummu.de/add.php").method(Method.POST);
			String coords = "";
			for(Coordinate c : w.getCoordinates()) {
				coords += c.toString() + ";";
			}
			con.data("coords", coords);
			con.data("tag", w.getValue("sur:tag"));
			con.data("standort",location.toString());
			
			try {
				con.execute();
			} catch (IOException e) {
				return -1;
			}
		}
		return 1;
	}
	


    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Integer status) {
    	if(status<0)
			Toast.makeText(v, v.getString(R.string.no_data_transmit), Toast.LENGTH_SHORT).show();
    	else
    		Toast.makeText(v, v.getString(R.string.data_transmit), Toast.LENGTH_SHORT).show();
    }

}
