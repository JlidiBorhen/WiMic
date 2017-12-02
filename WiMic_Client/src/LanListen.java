import java.io.IOException;
import java.net.InetAddress;
//import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

//import org.apache.commons.io.IOUtils;

public class LanListen {
	public static String hostIp="";
	public LanListen() throws IOException{
		isl.runListener();
	}

	private IncomingSoundListener isl = new IncomingSoundListener();    
	AudioFormat format = getAudioFormat();
	InputStream is;
	Socket client;
	int port=50005;
	boolean inVoice = true;


	private AudioFormat getAudioFormat(){
		float sampleRate = 48000.0F;
		int sampleSizeBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;

		return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
	}
	class IncomingSoundListener {
		public void runListener(){
			try{
				System.out.println("Connecting to server:"+hostIp+" Port:"+port);
				client = new Socket(hostIp,port); 
				System.out.println("Connected to: "+client.getRemoteSocketAddress());
				System.out.println("Listening for incoming audio.");
				
				
				DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class,format);
				SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
				speaker.open(format);
				speaker.start();
				while(inVoice){
					is = client.getInputStream();
					byte[] data= readFully(is);
					//byte[] data= IOUtils.toByteArray(is);
					ByteArrayInputStream bais = new ByteArrayInputStream(data);
					AudioInputStream ais = new AudioInputStream(bais,format,(long)data.length);
					int bytesRead = 0;
					if((bytesRead = ais.read(data)) != -1){
						System.out.println("Writing to audio output.");
						speaker.write(data,0,bytesRead);

						             //   bais.reset();
					}
					ais.close();
					bais.close();

				}
				speaker.drain();
				speaker.close();
				System.out.println("Stopped listening to incoming audio.");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public static void main(String [] args) throws IOException{
		checkHosts("192.168.1");
		new LanListen();
	}

	public static void checkHosts(String subnet){
		int timeout=5000;
		String hostPC="PC";


		try {
			if (InetAddress.getByName(hostPC).isReachable(timeout)){
				System.out.println(InetAddress.getByName(hostPC).getHostAddress()+" : "+hostPC + " is reachable");
				hostIp=InetAddress.getByName(hostPC).getHostAddress();
				//hostIp="192.168.1.4";
			
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public static byte[] readFully(InputStream input) throws IOException
	{
	    byte[] buffer = new byte[8192];
	    int bytesRead;
	    bytesRead = input.read(buffer);
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    output.write(buffer, 0, bytesRead);
	    return output.toByteArray();
	}
}


