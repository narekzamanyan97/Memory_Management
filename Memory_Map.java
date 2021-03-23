/***************************************************************
*file: Memory_Map.java
*authors: Narek Zamanyan, Jonathan Dunsmore
*class: CS 4310 – Operating Systems
*assignment: program 3
*date last modified: 05/11/2020
*
*purpose: This class implements the underlying structure of the memory, with all
  of its policies and algorithms
*
****************************************************************/

import java.util.ArrayList;

public class Memory_Map {
  static final int MAX_PROCESSES = 20;
  static final int MAX_MEMORY_SIZE = 30000;
  static final int MAX_TIME = 100000;

  //Memory Management Policy constants
  static final int VSP = 1;
  static final int PAG = 2;
  static final int SEG = 3;

  //algorithm used for VSP and SEG
  static final int FIRST_FIT = 1;
  static final int BEST_FIT = 2;
  static final int WORST_FIT = 3;

  static final int HOLE = -1;

  //this flag indicates that the SEG policy was unable to move all the process
  //segments into memory
  static final boolean MOVE_FAILED = false;



  public ArrayList<Integer> partition_pointer;

  public ArrayList<Integer> partition_owner;

  public ArrayList<Integer> partition_pag_or_seg_number;

  public int number_of_partitions;

  public int algorithm;

  public int policy;

  public int page_size;


  //Memory_Map constructor
  public Memory_Map(int memory_size, int policy, int algorithm_or_page_size){
    //initially, there is only one partition in the memory, which is a holef
    this.number_of_partitions = 1;

    this.policy = policy;
    if(policy == PAG) {
      this.page_size = algorithm_or_page_size;
    }
    else {
      this.algorithm = algorithm_or_page_size;
    }

    //the start of the address of the hole is 0 and the end is (memory_size - 1)
    this.partition_pointer = new ArrayList<Integer>();
    this.partition_pointer.add(0);
    this.partition_pointer.add(memory_size - 1);

    this.partition_owner = new ArrayList<Integer>();
    this.partition_owner.add(HOLE);

    this.partition_pag_or_seg_number = new ArrayList<Integer>();
    this.partition_pag_or_seg_number.add(HOLE);

    this.algorithm = algorithm;
  }



  /*
    ****************************************************************************
                                print memory map
    ****************************************************************************
  */
  public void print_memory_map() {
    System.out.println("    Memory Map: ");
    for(int i = 0; i < number_of_partitions; i++) {
      System.out.print("        " + this.partition_pointer.get(2*i) + "-" +
            this.partition_pointer.get(2*i + 1) + ": ");
      //check if the partition is a hole
      if(this.partition_owner.get(i) == HOLE) {
        //if the partition is a hole, check the policy
        if(this.policy == VSP || this.policy == SEG) {
          //if the policy is VSP, or SEG, then print the word hole
          System.out.print("Hole");
        }
        //if the policy is PAG, pring the word Free frame(s)
        else if(this.policy == PAG) {
          //if the policy is VSP, or SEG, then print the word hole
          System.out.print("Free frame(s)");
        }
      }
      //the partition is occupied by a process
      else {
          System.out.print("Process " + this.partition_owner.get(i));
          if(this.policy == PAG) {
            //pages start with 1, instead of 0 (as is the case with segments)
            System.out.print(", Page " + (this.partition_pag_or_seg_number.get(i) + 1));
          }
          else if(this.policy == SEG) {
            System.out.print(", Segment " + this.partition_pag_or_seg_number.get(i));
          }
      }
      //print a space
      System.out.println();
    }
    //print a space
    System.out.println();
  }



  /*
    ****************************************************************************
                                move_to_memory
    ****************************************************************************
  */
  //if there is no suitable partition, return -1
  //else, return the pointer of the partition's first address
    public boolean move_to_memory(Process p) {
      if(this.policy == VSP) {
        return move_to_memory_VSP(p);
      }
      else if(this.policy == PAG) {
        return move_to_memory_PAG(p);
      }
      else {
        return move_to_memory_SEG(p);
      }
    }

  /*
    ****************************************************************************
                                private methods for VSP policy
    ****************************************************************************
  */
  private boolean move_to_memory_VSP(Process p) {
    int process_size = p.get_total_process_size();
    int original_hole_end_address = 0;
    int original_hole_size = 0;
    int index_to_move = get_available_hole_pointer(p);

    if(index_to_move != -1) {
        //save the original_hole_size to be used later
        original_hole_size = this.partition_pointer.get(2*index_to_move + 1) -
                this.partition_pointer.get(2*index_to_move) + 1;

        //save the end address of the hole to be used for the
        //end address of the shrinked hole
        original_hole_end_address = this.partition_pointer.get(2*index_to_move + 1);

        //change the end address of the new partition to be process_size +
        //the start address of the hole - 1
        this.partition_pointer.set(2*index_to_move + 1,
              this.partition_pointer.get(2*index_to_move) + process_size - 1);

        //change the partition_owner from HOLE to the current process's id
        this.partition_owner.set(index_to_move, p.get_process_id());

        //if the process does not perfectly fit into the hole, we end up
        //creating 2 partitions: first one being allocated by the process,
        //the second one being the smaller version of the original hole
        if(process_size != original_hole_size) {

          //set the start address of the new smaller hole to be 1 + the end address
          //of the added page
          this.partition_pointer.add(2*(index_to_move + 1), this.partition_pointer.get(2*index_to_move + 1) + 1);

          //set the end address of the new smaller hole to be the end address
          //of the original hole saved previously
          this.partition_pointer.add(2*(index_to_move + 1) + 1, original_hole_end_address);

          //add a new partition owner
          this.partition_owner.add(index_to_move + 1, HOLE);

          //increment the number of partitions
          this.number_of_partitions++;
        }

        return true;

      }//endif index_to_move != -1

      //index_to_move = -1 (the process does not fit into the current memory's state)
      else {
        System.out.println("First_Fit: Not enough space to add the process id = " + p.get_process_id());
        return false;
      }
  }


  //helper method for move_to_memory_VSP
  //Return the index of the available hole (the start address of the hole)
  public int get_available_hole_pointer(Process p) {
    int process_size = p.get_total_process_size();
    int original_hole_end_address = 0;
    int original_hole_size = 0;
    //the index to add the new process, initially set to invalid index
    int index_to_move = -1;

    // System.out.println("algorithm = " + algorithm);

    if(algorithm == FIRST_FIT) {
      //loop through all the partition owners to find HOLE's
      for(int i = 0; i < number_of_partitions; i++) {
        if(this.partition_owner.get(i) == HOLE) {
          original_hole_size = this.partition_pointer.get(2*i + 1) -
                  this.partition_pointer.get(2*i) + 1;
          if(process_size <= original_hole_size) {
              index_to_move = i;
              return index_to_move;
          }
        }
      }
      return -1;
    }
    //algorithm == WORST_FIT
    else if(algorithm == WORST_FIT) {
      int size_difference = 0;
      int max_size_difference = -1;
        //loop through all the partition owners to find HOLEs
        for(int i = 0; i < this.number_of_partitions; i++) {
          //if this partition is a hole
          if(this.partition_owner.get(i) == HOLE) {
            original_hole_size = this.partition_pointer.get(2*i + 1) -
                    this.partition_pointer.get(2*i) + 1;
            if(process_size <= original_hole_size) {
                //save the size_difference to find the worst fit (most size difference)
                size_difference = original_hole_size - process_size;
                //if this partition yields a fit worse than the current worst fit
                if(max_size_difference < size_difference) {
                  //update the max_size_difference to be used in the following
                  //iterations
                  max_size_difference = size_difference;

                  //save the index_to_move to be used after the for loop (after
                  //the worst fit search is complete)
                  index_to_move = i;
                }
            }
          }
        }
        //return the worst fit partition's start address
        return index_to_move;
    }
    //algorithm == BEST_FIT
    else {
      int size_difference = 0;
      int min_size_difference = MAX_MEMORY_SIZE;

      //loop through all the partition owners to find HOLE's
      for(int i = 0; i < this.partition_owner.size(); i++) {
        if(this.partition_owner.get(i) == HOLE) {
          original_hole_size = this.partition_pointer.get(2*i + 1) -
                  this.partition_pointer.get(2*i) + 1;
          if(process_size <= original_hole_size) {
              size_difference = original_hole_size - process_size;
              //System.out.println("min_size_difference = " + min_size_difference);
              //System.out.println("size_difference = " + size_difference);
              if(min_size_difference > size_difference) {

                min_size_difference = size_difference;

                index_to_move = i;
              }
          }
        }
      }
      //System.out.println("index_to_move = " + index_to_move);
      return index_to_move;
    }
  }

  /*
    ****************************************************************************
                                private methods for PAG policy
    ****************************************************************************
  */

  private boolean move_to_memory_PAG(Process p){
    int original_hole_end_address = 0;
    int original_hole_size = 0;
    //if there is enough space for the process
    if(is_space_available_PAG(p)) {
      int process_size = p.get_total_process_size();

      //set the remaining pages to be the initial process size / page_size
      int remaining_pages = process_size / page_size;

      //Since a process with less memory than the page size will make the quotient
      //0, we need to add 1 to it.
      if(process_size % page_size != 0) {
        remaining_pages++;
      }

      //page number keeps track of the process's page numbers that are
      //added to the memory map one by one in the for loop
      int page_number = 0;

      for(int i = 0; remaining_pages > 0 && i < this.number_of_partitions; i++) {
        //check if the partition is a HOLE (not allocated to any page)
        if(this.partition_owner.get(i) == HOLE) {
          //get the current hole size
          original_hole_size = this.partition_pointer.get(2*i + 1) -
                  this.partition_pointer.get(2*i) + 1;
          //check if the page fits in the hole
          if(this.page_size <= original_hole_size) {
            //System.out.println("remaining_pages = " + remaining_pages);


            //save the end address of the current hole into a temporary variable,
            // so we can savely overwrite it (shrink it) with the end address of
            // the moving page
            original_hole_end_address = this.partition_pointer.get(2*i + 1);


            //the end address of the page becomes the start address of the hole
            // plus the page size minus 1
            this.partition_pointer.set(2*i + 1, this.partition_pointer.get(2*i) + page_size - 1);

            //change the owner of the hole to be the process that just got allocated
            //into the hole
            this.partition_owner.set(i, p.get_process_id());

            //update the partition_pag_seg with the page_number being moved in.
            // Then, increment the page_number
            //System.out.println("part_seg_or_pag = " + this.partition_pag_or_seg_number);
            this.partition_pag_or_seg_number.set(i, page_number++);

            //add the start address of the new hole
            //if the address of the hole is larger than the page size, then there
            //will be a smaller hole after the page is moved into the hole.
            //Otherwise, there will be no hole, and the page will perfectly fit
            //the hole, without a need to add a smaller hole to the right of the
            //page

            if(page_size != original_hole_size) {

              //set the start address of the new smaller hole to be 1 + the end address
              //of the added page
              this.partition_pointer.add(2*(i + 1), this.partition_pointer.get(2*i + 1) + 1);

              //set the end address of the new smaller hole to be the end address
              //of the original hole saved previously
              this.partition_pointer.add(2*(i + 1) + 1, original_hole_end_address);

              //add a new partition owner
              this.partition_owner.add(i + 1, HOLE);

              //add a new element in partition_pag_seg and set its value to HOLE
              this.partition_pag_or_seg_number.add(i + 1, HOLE);

              //increment the number of partitions
              this.number_of_partitions++;
            }

            //decrement the number of remaining pages, as this page just got moved in
            remaining_pages--;
          }
        }

        // System.out.println("original hole start address = " + this.partition_pointer.get(2*i));
        // System.out.println("original hole end address = " + original_hole_end_address);
        // System.out.println("i = " + i);
        // System.out.println("remaining pages = " + remaining_pages);
        // System.out.println("number of partitions = " + this.number_of_partitions);
        // System.out.println();
        // System.out.print("printing memory");
        // print_memory_map();
      }

      return true;
    }
    else {
      //System.out.println("Not enough space to add process with id = " + p.get_process_id());
      return false;
    }
  }

  //helper method for move_to_memory_PAG
  //return true if space is available. Otherwise, return false.
  private boolean is_space_available_PAG(Process p) {
    ArrayList<Integer> returned_array_list = new ArrayList<Integer>();
    int total = 0;
    for(int i = 0; i < this.partition_owner.size(); i++) {
      if(this.partition_owner.get(i) == HOLE) {
        total += this.partition_pointer.get(2*i + 1) - this.partition_pointer.get(2*i) + 1;
      }
    }
    //System.out.println("process total size = " + p.total_process_size);
    //System.out.println("total free space = " + total);
    if(p.total_process_size <= total) {
      return true;
    }
    else {
      return false;
    }
  }

    /*
      helper function for is_space_available
        what should we return? there are multiple indices needed to store the
        multiple segments
        also, when we add one segment to memory, the new hole can actually accomodate
        another smaller segment not yet accomodated. So this means we should move
        a process one segment at a time, then after the move, observe the memory map
        and continue according to the new version of the map
                                  solution
       return an array list, with the pointers allocating each segment to its
       appropriate hole. So the segments data structure in Process.java will
       be mapped to the return array_list
       so segments.get(i) will be mapped to returned_array_list.get(i)
       So the function will return all the address pointers, which will then be
       readily used by the
    */

    //returns an array list that tells which segment goes to which partition
    //if there is not enough space for the algorithm to move the process in,
    // the first element of the returned ArrayList will be set to -1
    private boolean move_to_memory_SEG(Process p) {

      /*
        save the initial state of the object. If the move is not successful, use
          this saved values to restore the data structures.
      */
      ArrayList<Integer> original_partition_pointer = new
            ArrayList<Integer>(this.partition_pointer);

      ArrayList<Integer> original_partition_owner = new
            ArrayList<Integer>(this.partition_owner);

      ArrayList<Integer> original_partition_pag_or_seg_number = new
            ArrayList<Integer>(this.partition_pag_or_seg_number);


      int original_number_of_partitions = this.number_of_partitions;


      //this will be returned after the function completes
      //ArrayList<Integer> indices = new ArrayList<Integer>();

      /*
        keeps track of the segment number of the process. Incremented every time
         a segment is moved into the meomory.
         All the segments with number < current_segment_number have been
         moved into memory
      */
      int hole_end_address = 0;
      int hole_start_address = 0;
      int hole_size = 0;
      int current_segment_size = 0;
      int current_segment_number = 0;

      if(algorithm == FIRST_FIT){
        /*
          iterate through all the partitions
          check if all the segments have been moved to the memory
          (current_segment_number < p.get_number_of_segments()) because there
          is no point to continue if there are no more segments
        */


          for(int i = 0; i < this.number_of_partitions && current_segment_number <
                p.get_number_of_segments(); i++) {

            //if the current partition is a hole
            if(this.partition_owner.get(i) == HOLE) {
              //store the current_segment_size into a variable for easy reference
              hole_size = this.partition_pointer.get(2*i + 1) -
                      this.partition_pointer.get(2*i) + 1;

              current_segment_size = p.get_segment_size(current_segment_number);

              //if the hole is able to store a segment.
              if(current_segment_size <= hole_size) {

                move_segment_to_memory(p, current_segment_number++, i);

                i = 0;

              }
            }
          }

      }
      else if(algorithm == WORST_FIT) {
        //initializing a varialbe worst_size to find the worst fit
        //System.out.println("WORST FIT");
        int size_difference = 0;
        int max_size_difference = -1;
        int index_to_move = -1;

        //this variable will be set to false if at least one partition is found in the
        //entire iteration of the for loop.
        boolean iteration_failed = false;

        //if iteration fails once, there is no reason to continue the search
        while(current_segment_number < p.get_number_of_segments() &&
                iteration_failed == false) {

          //reset the value to true.
          iteration_failed = true;

          //reset the max_size_difference before starting the for loop
          max_size_difference = -1;

          //check if there is need to traverse the
          for(int i = 0; i < this.number_of_partitions && current_segment_number <
                p.get_number_of_segments(); i++) {

            //if the current parition is a hole
            if(this.partition_owner.get(i) == HOLE) {
              //System.out.println("partition " + i + " is a hole");
              //store the current_segment_size into a variable for easy reference
              current_segment_size = p.get_segment_size(current_segment_number);
              hole_size = this.partition_pointer.get(2*i + 1) -
                      this.partition_pointer.get(2*i) + 1;

              //if the hole is able to store a segment.
              if(current_segment_size <= hole_size) {
                // System.out.println("current_segment " + current_segment_number +
                //         " has size less than hole_size");
                //iteration is successful (a hole was found)
                iteration_failed = false;
                //System.out.println("current segment size is <= hole size");
                //update the size difference between the hole and the segment
                size_difference = hole_size - current_segment_size;

                /*
                  if the current size difference is the most so far (if this
                    is a worst fit)
                */
                if(max_size_difference < size_difference) {

                  //update the max_size_difference with the current size_difference
                  max_size_difference = size_difference;

                  //update the index for the worst fit
                  index_to_move = i;
                  //System.out.println("index to move = " + index_to_move);
                }
              }//end if current_segment_size <= hole_size
            }//end if .. = HOLE
          }//end for

          //check if the iteration was successful
          if(iteration_failed == false) {

            move_segment_to_memory(p, current_segment_number, index_to_move);

            current_segment_number++;


          }

      }//end while
    }//end else if
    //algorithm == BEST_FIT
    else {
      int size_difference = 0;
      int min_size_difference = MAX_MEMORY_SIZE + 1;
      int index_to_move = -1;

      //this variable will be set to false if at least one partition is found in the
      //entire iteration of the for loop.
      boolean iteration_failed = false;

      // System.out.print("" + p.get_process_id() + "  ");
      // for(int k = 0; k < p.get_number_of_segments(); k++) {
      //   System.out.print(" " + p.get_segment_size(k));
      // }
      // System.out.println();

      //if iteration fails once, there is no reason to continue the search
      while(current_segment_number < p.get_number_of_segments() &&
              iteration_failed == false) {

        //reset the value to true.
        iteration_failed = true;

        //reset the max_size_difference before starting the for loop
        min_size_difference = MAX_MEMORY_SIZE + 1;

        //check if there is need to traverse the memory map
        for(int i = 0; i < this.number_of_partitions && current_segment_number <
              p.get_number_of_segments(); i++) {

          //if the current parition is a hole
          if(this.partition_owner.get(i) == HOLE) {


            //store the current_segment_size into a variable for easy reference
            current_segment_size = p.get_segment_size(current_segment_number);

            hole_size = this.partition_pointer.get(2*i + 1) -
                    this.partition_pointer.get(2*i) + 1;

            //while the hole is able to store a segment.

            if(current_segment_size <= hole_size) {
              //iteration is successful (a hole was found)
              iteration_failed = false;

              //update the size difference between the hole and the segment
              size_difference = hole_size - current_segment_size;

              /*
                if the current size difference is the most so far (if this
                  is a worst fit)
              */
              if(min_size_difference > size_difference) {

                //update the max_size_difference with the current size_difference
                min_size_difference = size_difference;

                /*
                  update the index for the worst fit to be used in the function
                    call mvoe_segment_to_memory
                */
                index_to_move = i;
              }
            }//end if current_segment_size <= hole_size
          }//end if .. = HOLE
        }//end for

        //check if the iteration was successful
        if(iteration_failed == false) {
          //call the function to move the segment to memory
          move_segment_to_memory(p, current_segment_number++, index_to_move);
        }

      }//end while
    }//endif

      //if all the segments were successfully moved into the memory, return true
      if(current_segment_number == p.get_number_of_segments()){
        return true;
      }

      //not all the segments were moved into memory
      else {
          //restore the original state of the memory
          this.partition_pointer = (ArrayList<Integer>) original_partition_pointer.clone();

          this.partition_owner = (ArrayList<Integer>) original_partition_owner.clone();

          this.partition_pag_or_seg_number = (ArrayList<Integer>) original_partition_pag_or_seg_number.clone();

          this.number_of_partitions = original_number_of_partitions;

          return MOVE_FAILED;
      }
  }

  //helper function for move_to_memory_SEG
  //moves the specified segment of the process to memory
  private void move_segment_to_memory(Process p, int current_segment_number, int i) {
    int hole_end_address = -1;
    int hole_start_address = -1;
    int hole_size = -1;
    int current_segment_size = p.get_segment_size(current_segment_number);
    /*
      save the endpoint addresses and the size  of the current hole
      into temporary variables,
      so we can savely overwrite it (shrink it) with the end address of
      the moving segment
    */
    hole_end_address = this.partition_pointer.get(2*i + 1);
    //save the start and end addresses of the current hole
    hole_start_address = this.partition_pointer.get(2*i);
    //save the size of the hole
    hole_size = this.partition_pointer.get(2*i + 1) -
          this.partition_pointer.get(2*i) + 1;

    //the end address of the segment becomes the start address of the hole
    // plus the segment size - 1
    this.partition_pointer.set(2*i + 1,
          hole_start_address + current_segment_size -1);

    //change the owner of the hole to be the process that just got allocated
    //into the hole
    this.partition_owner.set(i, p.get_process_id());

    //update the partition_pag_seg with the segment_number being moved in.
    // Then, increment the current_segment_number
    this.partition_pag_or_seg_number.set(i, current_segment_number);

    /*
      add the start address of the new hole
        if the address of the hole is larger than the page size, then there
        will be a smaller hole after the page is moved into the hole.
        Otherwise, there will be no hole, and the page will perfectly fit
        the hole, without a need to add a smaller hole to the right of the
        page
    */
    if(current_segment_size != hole_size) {

      //set the start address of the new smaller hole to be 1 + the end address
      //of the added segment
      this.partition_pointer.add(2*(i + 1), this.partition_pointer.get(2*i + 1) + 1);

      //set the end address of the new smaller hole to be the end address
      //of the original hole saved previously
      this.partition_pointer.add(2*(i + 1) + 1, hole_end_address);

      //add a new partition owner
      this.partition_owner.add(i + 1, HOLE);

      //add a new element in partition_pag_seg and set its value to HOLE
      this.partition_pag_or_seg_number.add(i + 1, HOLE);

      //increment the number of partitions
      this.number_of_partitions++;

      //update the hole endpoints
    }
  }


  /*
    ****************************************************************************
                                remove_from_memory
    ****************************************************************************
  */
  //!!! combine all the remove funcions into one

  //call this method when the process completes
  public boolean remove_from_memory(int p_id) {
    if(this.policy == VSP) {
      return remove_from_memory_VSP(p_id);
    }
    //policy is PAG or SEG
    else {
      return remove_from_memory_PAG_SEG(p_id);
    }
  }

private boolean remove_from_memory_VSP(int p_id) {
  int partition_number = -1;

  if(this.partition_owner.contains(p_id)) {
    for(int i = 0; i < this.number_of_partitions; i++) {
      if(this.partition_owner.get(i) == p_id) {

        //make the partition into a hole, so other processes can be stored in it
        this.partition_owner.set(i, HOLE);

        //save the partition number the process was found in, to be used during
        //mering
        partition_number = i;

        /*
          once we get the partition_number, we can quit the for loop, because
            VSP resides in only one partition
        */
        i = number_of_partitions;
      }
    }

    /*
      check to see if the process was in the last partition. If no, then it has
        an adjacent partition to its right
    */
    if(partition_number < this.number_of_partitions - 1) {
      this.merge_right_hole(partition_number);
    }

    /*
      check to see if the process was in the first partition. If no, then it has
      an adjacent partition to its left
    */
    if(partition_number > 0) {
      this.merge_left_hole(partition_number);
    }

    return true;
  }
  else {
    System.out.println("Process " + p_id + " is not in memory");
    return false;
  }
}


private boolean remove_from_memory_PAG_SEG(int p_id) {
  int partition_number = -1;

  if(this.partition_owner.contains(p_id)) {

    for(int i = 0; i < this.number_of_partitions; i++) {

      if(this.partition_owner.get(i) == p_id) {
        //make the partition into a hole, so other processes can be stored in it
        this.partition_owner.set(i, HOLE);

        //make the partition_pag_or_seg into a hole as well
        this.partition_pag_or_seg_number.set(i, HOLE);

        //save the partition number the process was found in, to be used during
        //merging
        partition_number = i;

        /*
          check to see if the process was in the first partition. If no, then it has
            an adjacent partition to its left
        */

        if(partition_number > 0) {
          if(merge_left_hole(partition_number)) {
            partition_number--;
            i--;
          }

        }

        if(partition_number < this.number_of_partitions - 1) {
          merge_right_hole(partition_number);
        }

      }
    }

    return true;
  }
  else {
    System.out.println("Process " + p_id + " is not in memrory");
    return false;
  }
  //merge the holes
}

/*
  ****************************************************************************
                              merge_right/left_holes
  ****************************************************************************
*/

private boolean merge_right_hole(int partition_number) {
  /*
    set the right adjacent partition number to be 1 larger than the current
      partition number
  */
  int right_adjacent_partition_number = partition_number + 1;

  boolean merge_was_successful = false;
  // System.out.println("*****************");
  // System.out.println(this.partition_pointer.get(2*partition_number));
  // System.out.println("*****************");
  //if the right adjacent partition is a hole, then merge with this hole
  if(this.partition_owner.get(right_adjacent_partition_number) == HOLE) {

    /*
      set the end address of the current hole to point to the end address of
        the right adjacent hole
    */
    this.partition_pointer.set(2*partition_number + 1,
          this.partition_pointer.get(2*right_adjacent_partition_number + 1));

    /*
      remove the right adjacent hole (its start and end addresses) from
        partition_pointer.
    */
    this.partition_pointer.remove(2*right_adjacent_partition_number);
    this.partition_pointer.remove(2*right_adjacent_partition_number);

    /*
      remove the partition of the right adjacent hole from partition_owner
    */
    this.partition_owner.remove(right_adjacent_partition_number);

    /*
      remove the right page or segment hole from the partition_pag_or_seg_number
    */
    if(policy == PAG || policy == SEG) {
      this.partition_pag_or_seg_number.remove(right_adjacent_partition_number);
    }

    //decrement the number of partitions
    this.number_of_partitions--;
    merge_was_successful = true;
  }
  else {
    merge_was_successful = false;
  }
  return merge_was_successful;
}

private boolean merge_left_hole(int partition_number) {
  boolean merge_was_successful = false;
  /*
    set the end address of the current hole to point to the end address of
      the right adjacent hole
  */
  int left_adjacent_partition_number = partition_number - 1;

  //if the left adjacent partition is a hole, then merge with this hole
  if(this.partition_owner.get(left_adjacent_partition_number) == HOLE) {
    /*
      set the start address of the current hole to point to the start address of
        the left adjacent hole
    */
    this.partition_pointer.set(2 * partition_number,
          this.partition_pointer.get(2*left_adjacent_partition_number));

    /*
      remove the left adjacent hole (its start and end addresses) from
        partition_pointer.
    */
    this.partition_pointer.remove(2*left_adjacent_partition_number);
    this.partition_pointer.remove(2*left_adjacent_partition_number);

    /*
      remove the partition of the right adjacent hole from partition_owner
    */
    this.partition_owner.remove(left_adjacent_partition_number);

    /*
      remove the left page or segment hole from the partition_pag_or_seg_number
    */
    if(policy == PAG || policy == SEG) {
      this.partition_pag_or_seg_number.remove(left_adjacent_partition_number);
    }

    //decrement the number of partitions
    this.number_of_partitions--;
    merge_was_successful = true;
  }
  else {
    merge_was_successful = false;
  }
  return merge_was_successful;
}
}
