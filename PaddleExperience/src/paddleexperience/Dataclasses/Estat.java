/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paddleexperience.Dataclasses;

// Java imports
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

// Internal imports
import model.Member;
import DBAcess.ClubDBAccess;
import model.Court;
import paddleexperience.FXMLSidebarController;

/**
 *
 * @author saisua
 */
public final class Estat {
    // Instancia de ClubDBAccess per poder accedir a la mateixa instància
    // amb mètodes estàtics desde tot el projecte
    public static final ClubDBAccess club = ClubDBAccess.getSingletonClubDBAccess();
    
    // Guardar informació d'estat
    private static Member member;
    private static FXMLSidebarController sidebar;
    private static LocalTime time = LocalTime.NOON;
    private static LocalDate date;
    
    // Variables límit
    public static final int partides_duracio = club.getClubBookingDuration();
    public static final int partides_dia = club.getClubBookingSlots();
    public static final int numero_pistes = club.getCourts().size();
    private static final int __time_inici = 540; // 9 am
    
    public static final LocalTime time_inici = LocalTime.of(__time_inici/60, __time_inici%60); // 9 am
    public static final LocalTime time_final = time_inici.plus(partides_duracio*(partides_dia-1), 
                                                                ChronoUnit.MINUTES);
    
    // Auxiliars
    public static final HashMap<Court, Integer> court_index = new HashMap<Court, Integer>(){{
            for(int court_num = 0; court_num < club.getCourts().size(); court_num++) 
                    put(club.getCourts().get(court_num), court_num);
            }};
    
    // // STATE METHODS
    
    public static void save(){
        if(club.saveDB())
            System.out.println("Base de dades actualitzada!");
        else{
            System.out.println("La base de dades no ha sigut actualitzada. Tornant a intentar...");
            if(club.saveDB())
                System.out.println("Base de dades actualitzada!");
            else
                System.out.println("[ERROR] La base de dades no pot ser actualitzada");
        }
    }
    
    
    // // GETTERS
    
    public static ClubDBAccess getClub(){
        return club;
    }
    
    public static Member getMember(){
        return member;
    }
    
    public static LocalDate getDate(){
        if(date == null) return LocalDate.now();
        
        return date;
    }
    
    public static LocalDate getNextDate(){
        return getDate().plusDays(1);
    }
    
    public static LocalDate getBeforeDate(){
        return getDate().minusDays(1);
    }
    
    public static LocalTime getTime(){
        if(time == null) return LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        
        return time;
    }
    
    public static LocalTime getNextTime(){
        LocalTime result = getTime().plusMinutes(partides_duracio);
        
        if(result.compareTo(time_final) > 0 || result.compareTo(time_inici) < 0) 
            return null;
        
        return result;
    }
    
    public static LocalTime getBeforeTime(){
        LocalTime result = getTime().minusMinutes(partides_duracio);
        
        if(result.compareTo(time_inici) < 0 || result.compareTo(time_final) > 0) 
            return null;
        
        return result;
    }
    
    public static LocalTime getInitialTime(){
        return time_inici;
    }
    
    public static int getInitialTimeint(){
        return __time_inici;
    }
    
    public static FXMLSidebarController getSidebar(){
        return sidebar;
    }
    
    // // SETTERS
    
    public static void setMember(Member member_set){
        member = member_set;
    }
    
    public static void setDate(LocalDate new_date){
        date = new_date;
    }
    
    public static void setTime(LocalTime new_time){
        time = new_time.truncatedTo(ChronoUnit.MINUTES);
    }
    
    public static void setSidebar(FXMLSidebarController new_sidebar){
        sidebar = new_sidebar;
    }
}
