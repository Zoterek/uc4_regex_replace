package pl.automic.implementation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Writer {
	boolean verbose = false;
	PrintWriter pw;
	
	public Writer(String path) throws IOException {
		FileWriter fw = new FileWriter(path, true);
		BufferedWriter bw = new BufferedWriter(fw);
		this.pw = new PrintWriter(bw);
	}
	
	public Writer(String path, boolean verbose) throws IOException {
		new File(path).delete();
		
		FileWriter fw = new FileWriter(path, true);
		BufferedWriter bw = new BufferedWriter(fw);
		this.pw = new PrintWriter(bw);
		this.verbose = verbose;
	}
	
	public void close() {
		pw.close();
	}
		
	public void log(String... args) {
		for(String arg : args) {
			pw.print(arg + " ");
			
			if(this.verbose) {
				System.out.print(arg + " ");
			}
		}
		pw.println("");
		
		if(this.verbose) {
			System.out.println("");
		}
	}
}
