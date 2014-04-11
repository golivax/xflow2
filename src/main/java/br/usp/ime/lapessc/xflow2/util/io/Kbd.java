package br.usp.ime.lapessc.xflow2.util.io;
import java.io.IOException;

public class Kbd{

	/*
	 * Método para ler uma String
	 */
	 
	public static String readString(){
		byte b[]=new byte[256];
		try {
			System.in.read(b);			
		}catch (IOException e){
		}
		String s=new String(b);
		s=s.trim();
		return(s);			
	}
	
	/*
	 * Método para ler uma String e emitir mensagem
	 */
	public static String readString(String m){
		System.out.print(m);
		return readString();
	}
	
	/*
	 * Método para ler um char
	 */
	public static char readChar(){
		return readString().charAt(0);			
	}
	
	/*
	 * Método para ler um char e emitir mensagem
	 */
	public static char readChar(String m){
		System.out.print(m);
		return readChar();
	}
	
	/*
	 * Método para ler um boolean
	 */
	public static boolean readBoolean(){
		boolean ret;
		ret=false;
		switch (readChar()) {
			case 's':
			case 'S':
			case 't':
			case 'T':
			case 'v':
			case 'V':
			case 'y':
			case 'Y':
			case '1': 
				ret=true;				
		}
		return (ret);			
	}
	
	/*
	 * Método para ler um boolean e emitir mensagem
	 */
	public static boolean readBoolean(String m){
		System.out.print(m);
		return readBoolean();			
	}
	
	/*
	 * Método para ler um inteiro
	 */
	public static int readInt(){
		int i=0;
		try {
			i=Integer.parseInt(readString());
		}catch(NumberFormatException e){
		}
		return(i);			
	}
	
	/*
	 * Método para ler um inteiro e emitir mensagem
	 */
	public static int readInt(String m){
		System.out.print(m);
		return readInt();			
	}		
	
	/*
	 * Método para ler um long int
	 */
	public static long readLong(){
		long i=0;
		try {
			i=Long.parseLong(readString());
		}catch(NumberFormatException e){
		}
		return(i);			
	}
	
	/*
	 * Método para ler um read long e emitir mensagem
	 */
	public static long readLong(String m){
		System.out.print(m);
		return readLong();			
	}
	
	/*
	 * Método para ler um short int
	 */
	public static short readShort(){
		short i=0;
		try {
			i=Short.parseShort(readString());
		}catch(NumberFormatException e){
		}
		return(i);			
	}
	
	/*
	 * Método para ler um short int e emitir mensagem
	 */
	public static short readShort(String m){
		System.out.print(m);
		return readShort();			
	}		
	
	/*
	 * Método para ler um byte
	 */
	public static byte readByte(){
		byte i=0;
		try {
			i=Byte.parseByte(readString());
		}catch(NumberFormatException e){
		}
		return(i);			
	}
	
	/*
	 * Método para ler um byte e emitir mensagem
	 */
	public static byte readByte(String m){
		System.out.print(m);
		return readByte();			
	}
	
	/*
	 * Método para ler um float
	 */
	public static float readFloat(){
		float i=0;
		try {
			i=Float.parseFloat(readString());
		}catch(NumberFormatException e){
		}
		return(i);			
	}
	
	/*
	 * Método para ler um float e emitir mensagem
	 */
	public static float readFloat(String m){
		System.out.print(m);
		return readFloat();			
	}
		
	/*
	 * Método para ler um Double
	 */
	public static double readDouble(){
		double i=0;
		try {
			i=Double.parseDouble(readString());
		}catch(NumberFormatException e){
		}
		return(i);			
	}
	
	/*
	 * Método para ler uma double e emitir mensagem
	 */
	public static double readDouble(String m){
		System.out.print(m);
		return readDouble();			
	}
}