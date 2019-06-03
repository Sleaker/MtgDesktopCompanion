package org.beta.fs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MTGPath implements Path {

	private FileSystem fs;
	private URI uri;
	private List<String> parts;

	
	public String toString()
	{
		return String.join(fs.getSeparator(), parts);
	}
	
	public MTGPath(FileSystem fs,String first, String... more) 
	{
		init(fs,first,more);
	}
	
	private void init(FileSystem fs, String first, String... more) 
	{
		this.fs=fs;
		parts = new ArrayList<>();
	
		if(first!=null) 
			parts.addAll(Arrays.asList(StringUtils.split(first,fs.getSeparator(),-1)));
		
		if(more!=null)
			parts.addAll(Arrays.asList(more));
	}

	public List<String> getParts() {
		return parts;
	}
	
	@Override
	public FileSystem getFileSystem() {
		return fs;
	}

	@Override
	public boolean isAbsolute() {
		return parts.get(0).equals(fs.getSeparator());
	}

	@Override
	public Path getRoot() {
		if (parts ==  null) {
            return this;
        }
		return new MTGPath(fs,parts.get(0));
	}

	@Override
	public Path getFileName() {
		return this;
	}

	@Override
	public Path getParent() {
		if (parts == null) {
	        return null;
        }
		return new MTGPath(fs, parts.get(getNameCount()-1));
	}

	@Override
	public int getNameCount() {
		return parts.size();
	}
	
	public String getStringFileName()
	{
		return parts.get(parts.size()-1);
	}

	@Override
	public Path getName(int index) {
		return new MTGPath(fs,parts.get(index));
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		List<String> l = parts.subList(beginIndex, beginIndex+endIndex);
		return new MTGPath(fs,beginIndex == 0 ? parts.get(0) : null, l.toArray(new String[l.size()]) );
	}

	@Override
	public boolean startsWith(Path other) {
		
		boolean ret = false;
		
		if(other instanceof MTGPath)
		{
			List<String> l = ((MTGPath)other).getParts();
			for(int i=0;i<=l.size()-1;i++)
			{
				ret = l.get(i).equalsIgnoreCase(parts.get(i));
			}
			
			return ret;
			
		}
		return ret;
	}

	@Override
	public boolean endsWith(Path other) {
		boolean ret = false;
		
		if(other instanceof MTGPath)
		{
			List<String> l = ((MTGPath)other).getParts();
			for(int i=l.size()-1;i>=0;i--)
			{
				ret = l.get(i).equalsIgnoreCase(parts.get(i));
			}
			return ret;
		}
		return ret;
	}

	@Override
	public Path normalize() {
		try {
			return new MTGPath(fs, new URI(toString()).normalize().toString());
		} catch (URISyntaxException e) {
			return null;
		}

	}

	@Override
	public Path resolve(Path other) {
		return new MTGPath(fs, toString(), ((MTGPath)other).toString());
	}

	@Override
	public Path relativize(Path other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI toUri() {
		try {
			return new URI(toString());
		} catch (URISyntaxException e) {
			return null;
		}
	}

	@Override
	public Path toAbsolutePath() {
		return null;
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(Path other) {
		return this.compareTo(other);
	}

}