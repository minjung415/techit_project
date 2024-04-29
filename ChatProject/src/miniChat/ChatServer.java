package miniChat;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatServer {
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(12345);
        ){
            System.out.println("서버가 준비되었습니다.");
            Map<String, PrintWriter> chatClients = new HashMap<>();
            Map<Integer, Map<String, PrintWriter>> chatRooms = new HashMap<>();
            while(true) {
                Socket socket = serverSocket.accept();
                new ChatThread(socket, chatClients, chatRooms).start();
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}

class ChatThread extends Thread{
    private Socket socket;
    private String id;
    private Map<String, PrintWriter> chatClients;
    private Map<Integer, Map<String, PrintWriter>> chatRooms;
    private BufferedReader in = null;
    private PrintWriter out;
    private int roomNum;
    public ChatThread(Socket socket, Map<String, PrintWriter> chatClients, Map<Integer, Map<String, PrintWriter>> chatRooms){
        this.socket = socket;
        this.chatClients = chatClients;
        this.chatRooms = chatRooms;
        try{
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(true) {
                id = in.readLine();
                if(chatClients.containsKey(id))
                    out.println("이미 존재하는 닉네임입니다. 다시 입력해주세요. : ");
                else
                    break;
            }
            System.out.print(id + "닉네임의 사용자가 연결했습니다.");
            System.out.println(" (" + this.socket.getInetAddress().getHostAddress() + ")");
            broadcast(id + "님이 입장하셨습니다.");
            out.println("방 목록 보기 : /list\n" +
                    "방 생성 : /create\n" +
                    "방 입장 : /join [방번호]\n" +
                    "방 나가기 : /exit\n" +
                    "사용자 목록 : /users\n" +
                    "방 사용자 목록 : /roomusers\n" +
                    "접속종료 : /bye\n" +
                    "귓속말 : /whisper [닉네임]\n");
            synchronized (chatClients){
                chatClients.put(id, out);
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        String msg = null;
        try{
            while ((msg = in.readLine()) != null){
                if("/bye".equalsIgnoreCase(msg)){
                    System.out.println(id + "닉네임의 사용자가 연결을 끊었습니다.");
                    break;
                }
                if("/list".equalsIgnoreCase(msg)){
                    if(chatRooms.isEmpty())
                        out.println("생성된 방이 없습니다.");
                    else {
                        out.println("방 목록");
                        for (int i = 1; i <= chatRooms.size(); i++) {
                            out.println(i);
                        }
                    }
                }
                else if("/create".equalsIgnoreCase(msg)){
                    if(isInRoom(id) == -1) {
                        chatRooms.put(chatRooms.size() + 1, new HashMap<>());
                        chatRooms.get(chatRooms.size()).put(id, out);
                        out.println("방이 생성되었습니다.");
                    }
                    else
                        out.println("방에서 나온 후 새로운 방을 생성해주세요.");
                }
                else if (msg.startsWith("/join")){
                    if(isInRoom(id) == -1) {
                        joinRoom(msg);
                    }
                    else
                        out.println("방에서 나온 후 다른 방에 입장해주세요.");
                }
                else if(msg.startsWith("/whisper")){
                    whisper(msg);
                }
                else if("/exit".equalsIgnoreCase(msg)){
                    if((roomNum = isInRoom(id)) == -1){
                        out.println("방에 있지 않습니다.");
                    }
                    else
                        exitRoom();
                }
                else if(msg.startsWith("/invite")){
                    if((roomNum = isInRoom(id)) == -1){
                        out.println("방에 있지 않습니다.");
                    }
                    else
                        inviteRoom(msg);
                }
                else if ("/users".equalsIgnoreCase(msg)){
                    for(String s : chatClients.keySet()){
                        out.println(s);
                    }
                }
                else if ("/roomusers".equalsIgnoreCase(msg)){
                    if((roomNum = isInRoom(id)) != -1) {
                        for (String s : chatRooms.get(roomNum).keySet()) {
                            out.println(s);
                        }
                    }
                    else
                        out.println("방에 있지 않습니다.");
                }
                else{
                    broadcast(id + " : " + msg);
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }finally {
            synchronized (chatClients){
                chatClients.remove(id);
            }
            broadcast(id + "님이 나갔습니다.");
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(socket != null){
                try {
                    socket.close();
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public void broadcast(String msg){
        //만약 방 안에 없다면
        if((roomNum = isInRoom(id)) == -1 ) {
            synchronized (chatClients) {
                Iterator<PrintWriter> iter = chatClients.values().iterator();
                while (iter.hasNext()) {
                    PrintWriter out = iter.next();
                    try {
                        out.println(msg);
                    } catch (Exception e) {
                        iter.remove();
                        System.out.println(e);
                    }
                }
            }
        }
        else {
            synchronized (chatRooms) {
                Iterator<PrintWriter> iter = chatRooms.get(roomNum).values().iterator();
                while (iter.hasNext()) {
                    PrintWriter out = iter.next();
                    try {
                        out.println("[room" + roomNum + "]" + msg);
                    } catch (Exception e) {
                        iter.remove();
                        System.out.println(e);
                    }
                }
            }
        }
    }
    public void whisper(String msg){
        int firstSpaceIndex = msg.indexOf(" ");
        if(firstSpaceIndex == -1)
            return;
        int secondSpaceIndex = msg.indexOf(" ", firstSpaceIndex + 1);
        if(secondSpaceIndex == -1)
            return;
        String to = msg.substring(firstSpaceIndex + 1, secondSpaceIndex);
        String message = msg.substring(secondSpaceIndex + 1);
        PrintWriter out = chatClients.get(to);
        if(out != null){
            out.println("[귓속말]" + id + " : " + message);
        }else {
            System.out.println("닉네임 '" + to + "'는(은) 존재하지 않습니다.");
        }
    }
    public void joinRoom(String msg){
        int firstSpaceIndex = msg.indexOf(" ");
        if(firstSpaceIndex == -1)
            return;
        int room = Integer.parseInt(msg.substring(firstSpaceIndex + 1));
        if(chatRooms.containsKey(room)) {
            chatRooms.get(room).put(id, out);
            broadcast(id + "님이 방에 입장하셨습니다.");
        }
        else
            out.println("존재하지 않는 방입니다.");
    }
    public void exitRoom(){
        broadcast(id + "님이 방에서 나갔습니다.");
        chatRooms.get(roomNum).remove(id);
        out.println(roomNum + "번방에서 나왔습니다.");
        if (chatRooms.get(roomNum).isEmpty())
            chatRooms.remove(roomNum);
    }
    public void inviteRoom(String msg){
        //초대할 사람을 찾는 코드
        int firstSpaceIndex = msg.indexOf(" ");
        if(firstSpaceIndex == -1)
            return;
        String name = msg.substring(firstSpaceIndex + 1);
        PrintWriter out = chatClients.get(name);
        //만약 존재하는 사람이면
        if(out != null){
            int roomnumber = isInRoom(name);
            //다른 방에 존재하는 사람이면
            if(roomnumber != -1){
                //다른 방에서 내보내고
                for(PrintWriter w : chatRooms.get(roomnumber).values()){
                    w.println("[room" + roomnumber + "]" +name + "님이 방에서 나갔습니다.");
                }
                chatRooms.get(roomnumber).remove(name);
                out.println(roomnumber + "번방에서 나왔습니다.");
                if (chatRooms.get(roomnumber).isEmpty())
                    chatRooms.remove(roomnumber);
            }
            //방에 초대
            chatRooms.get(isInRoom(id)).put(name, out);
            broadcast(id + "님이 " + name +"님을 방에 초대했습니다.");
        }
        else
            this.out.println("닉네임 " + name + "은 존재하지 않습니다.");
    }
    public int isInRoom(String name){
        for(int i = 1; i <= chatRooms.size(); i++) {
            if ((chatRooms.get(i)).containsKey(name)){
                return i;
            }
        }
        return -1;
    }
}
