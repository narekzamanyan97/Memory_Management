/***************************************************************
*file: Process.java
*authors: Narek Zamanyan, Jonathan Dunsmore
*class: CS 4310 – Operating Systems
*assignment: program 3
*date last modified: 05/11/2020
*
*purpose: This class acts as a data structure to easily work with processes
*
****************************************************************/


import java.util.ArrayList;

public class Process {
  public int process_id;
  public int arrival_time;
  public int lifetime;
  public int memory_entrance_time;
  public int number_of_segments;
  public ArrayList<Integer> segments;
  public int total_process_size;

  public Process() {
    process_id = 0;
    arrival_time = 0;
    memory_entrance_time = 0;
    lifetime = 0;
    number_of_segments = 0;
    segments = new ArrayList<Integer>();
    total_process_size = 0;
  }

  public int get_process_id() {
    return this.process_id;
  }

  public int get_arrival_time() {
    return this.arrival_time;
  }

  public int get_lifetime() {
    return this.lifetime;
  }

  public int get_process_memory_entrance_time() {
    return this.memory_entrance_time;
  }


  public int get_number_of_segments() {
    return this.number_of_segments;
  }

  public int get_segment_size(int segment_number) {
    if(segment_number < this.number_of_segments) {
      return segments.get(segment_number);
    }
    else if(segment_number < 0) {
      System.out.println("Please select a positive segment number");
      return -1;
    }
    else {
      System.out.println("This process has only " + this.number_of_segments
            + " segments.");
      return -1;
    }
  }

  public int get_total_process_size() {
    return this.total_process_size;
  }


  public boolean set_process_id(int p_id) {
    this.process_id = p_id;
    return true;
  }

  public boolean set_arrival_time(int p_arrival){
    this.arrival_time = p_arrival;
    return true;
  }
  public boolean set_lifetime(int p_lifetime) {
    this.lifetime = p_lifetime;
    return true;
  }

  public boolean set_memory_entrance_time(int time) {
    this.memory_entrance_time = time;
    return true;
  }

  public boolean add_segment(int a_segment) {
    this.segments.add(a_segment);
    //increase the total size by the new segment's size
    this.total_process_size += a_segment;
    //increment the number_of_segments by 1
    this.number_of_segments++;
    return true;
  }
}
