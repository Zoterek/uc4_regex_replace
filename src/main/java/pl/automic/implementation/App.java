package pl.automic.implementation;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import com.uc4.api.SearchResultItem;
import com.uc4.communication.requests.CloseObject;
import com.uc4.communication.requests.SaveObject;

import pl.automic.Automic;
import pl.automic.State;
import pl.automic.communication.requests.OpenObject;
import pl.automic.Replace;
import pl.automic.communication.requests.SearchObject;

public class App {
	Automic automic;
	boolean err;
	File config;
	File filter;
	Replace replace;
	Writer writer;

	public App(String[] args) {
		String path = readArg(2, args, getCurrentTime() + ".replace_regex_process.log");
		this.config = new File(readArg(0, args, "config.json"));
		this.filter = new File(readArg(1, args, "filter.json"));
		
		try {
			this.automic = new Automic(config);
			this.replace = new Replace(filter);
			this.writer = new Writer(path, true);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		App app = new App(args);
		
		Iterator<SearchResultItem> result = app.search();
		result.forEachRemaining(e -> app.replace(e.getName()));
		
		app.exit();
	}
	
	private void exit() {
		this.writer.close();
		
		if(this.err) {
			System.out.println("");
			System.out.println("Check log for error/skipped messages.");
		}
		
		try {
			this.automic.exit();
		} catch (IOException e) {
			
		}
	}
	
	private String getCurrentTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
	}
	
	private Iterator<SearchResultItem> search() {
		try {
			SearchObject so = new SearchObject(filter);

			so.selectAllObjectTypes();
			// TODO Temp patch for folders
			so.setTypeFOLD(false);
			
			System.out.println("Awaiting response ...");
			automic.send(so);
			
			System.out.println("Received search result.");
			return so.resultIterator();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String readArg(int pos, String[] args, String def) {
		return args.length > pos ? args[pos] : def;
	}
	
	private void replace(String objectName) {
		State state = State.OK;
		try {
			OpenObject open = new OpenObject(objectName);
			automic.send(open);
			writer.log(len12("OPENED:"), objectName);
			
			state = this.replace.process(open.getUC4Object());
			
			if(state == State.CHANGED) {
				SaveObject save = new SaveObject(open.getUC4Object());
				automic.send(save);
			} else if(state == State.SKIPPED) {
				this.err = true;
			}
			
			CloseObject close = new CloseObject(open.getUC4Object());
			automic.send(close);
			
		} catch (RuntimeException | IOException e) {
			this.err = true;
			state = State.ERROR;
		}

		writer.log(len12(state.name() + ":"), objectName);
	}
	
	private String len12(String str) {
		return String.format("%1$-12s", str);
	}

}
