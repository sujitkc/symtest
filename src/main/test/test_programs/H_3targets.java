package test;

import java.util.Random;

public class H_3targets {
	static void f_original(int x, int max_count)
	{
		 int y = 0;
		 int c = 0;
		 while ((x>10)&&(x<110)&&(c<max_count)) {
	        	if (x > 50)
	        	{
	        		y = y + 2;
	        	}
	        	else
	        	{
	        		y = y * 2;
	        	}
	        	if((y>100) && (y<115))
	        	{
	        		x = x + 2;
	        	}
	        	else
	        	{
	        		if((y>60) && (y<70))
	        		{
	        			x = x + 15;
	        		}
	        		else
	        		{
	        			
	        			if(y>65)
		        		{
		        			x = x + 1;
		        		}
	        			else
	        			{
	        				x = x+ 25;
	        			}
	        		}

	        	}
	        	if((x < 50)&&(x>47))
	        	{
	        		System.out.println("yes");
	        		x = x + 65;
	        	}
	        	else
	        	{
	        		System.out.println("no");
	        		x = x + 20;
	        	}
	        	c++;
	        }
	
    }
	
	
static void f_instrumented(int max_count, int x) {
		System.out.print("BEGIN->BB1 ");
		 int y = 0;
		 int c = 0;
		 System.out.print("BB1->BB2 ");
		 while ((x>10)&&(x<110)&&(c<max_count)) {
			 System.out.print("BB2->D3 ");
	        	if (x > 50)
	        	{
	        		System.out.print("D3->BB4 ");
	        		y = y + 1;
	        		System.out.print("BB4->BB6 ");
	        	}
	        	else
	        	{
	        		System.out.print("D3->BB5 ");
	        		y = y * 2;
	        		System.out.print("BB5->BB6 ");
	        	}
	        	System.out.print("BB6->D7 ");
	        	if((y>100) && (y<115))
	        	{
	        		System.out.print("D7->BB8 ");
	        		x = x + 2;
	        		System.out.print("BB8->BB18 ");
	        	}
	        	else
	        	{
	        		System.out.print("D7->BB9 ");
	        		System.out.print("BB9->D10 ");
	        		if((y>60) && (y<70))
	        		{
	        			System.out.print("D10->BB11 ");
	        			x = x + 15;
	        			System.out.print("BB11->BB18 ");
	        		}
	        		else
	        		{
	        			System.out.print("D10->BB12 ");
	        			System.out.print("BB12->D13 ");
	        			//x = x - 10;
	        			if(y>65)
		        		{
	        				System.out.print("D13->BB14 ");
		        			x = x + 1;
		        			System.out.print("BB14->BB16 ");
		        		}
	        			else
	        			{
	        				System.out.print("D13->BB15 ");
	        				x = x+ 5;
	        				System.out.print("BB15->BB16 ");
	        			}
	        			System.out.print("BB16->BB17 ");
	        		}
	        		System.out.print("BB8->BB18 ");

	        	}
	        	System.out.print("BB18->D19 ");
	        	if((x < 50)&&(x>47))
	        	{
	        		System.out.print("D19->BB20 ");
	        		x = x + 65;
	        		System.out.print("BB20->BB22 ");
	        	}
	        	else
	        	{
	        		System.out.print("D19->BB21 ");
	        		x = x + 20;
	        		System.out.print("BB21->BB22 ");
	        	}
	        	System.out.print("BB22->WHILE ");
	        	System.out.print("WHILE->BB2 ");
	        	c++;
	           }
		// System.out.print("e22 ");
		 }
	
		
	
static void f_instrumented1(int max_count, int x) {
		System.out.print("e1 ");
		 int y = 0;
		 int c = 0;
		// System.out.print("e2 ");
		 while ((x>10)&&(x<110)&&(c<max_count)) {
			 System.out.print("e2 ");
	        	if (x > 50)
	        	{
	        		System.out.print("e3 ");
	        		y = y + 1;
	        		System.out.print("e4 ");
	        	}
	        	else
	        	{
	        		System.out.print("e5 ");
	        		y = y * 2;
	        		System.out.print("e6 ");
	        	}
	        	if((y>100) && (y<115))
	        	{
	        		System.out.print("e7 ");
	        		x = x + 2;
	        		System.out.print("e8 ");
	        	}
	        	else
	        	{
	        		System.out.print("e9 ");
	        		if((y>60) && (y<70))
	        		{
	        			System.out.print("e10 ");
	        			x = x + 15;
	        			System.out.print("e11 ");
	        		}
	        		else
	        		{
	        			System.out.print("e12 ");
	        			//x = x - 10;
	        			if(y>65)
		        		{
	        				System.out.print("e13 ");
		        			x = x + 1;
		        			System.out.print("e14 ");
		        		}
	        			else
	        			{
	        				System.out.print("e15 ");
	        				x = x+ 5;
	        				System.out.print("e16 ");
	        			}
	        			//System.out.print("e1 ");
	        		}
	        		//System.out.print("e1 ");

	        	}
	        	if((x < 50)&&(x>47))
	        	{
	        		System.out.print("e17 ");
	        		x = x + 65;
	        		System.out.print("e18 ");
	        	}
	        	else
	        	{
	        		System.out.print("e19 ");
	        		x = x + 20;
	        		System.out.print("e20 ");
	        	}
	        	System.out.print("e21 ");
	        	c++;
	           }
		 System.out.print("e22 ");
		 }
	

public static void main(String args[]) {
	  int count = 5;
	  Random random = new Random();
	  for(int i = 0; i < 1000; i++) {
		  System.out.println(); 
	    int x = random.nextInt(50);
	    //System.out.println(x);
	    f_instrumented(count, x);
	   // f_original(24,100);
	  }
	  
	}

}
