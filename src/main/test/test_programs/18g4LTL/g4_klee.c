void pgm(int a, int b, int c,int d,int e)
{
	int motion_detector; 
	int window_sensor; 
	int master_switch;
	int deactivate_system;
	int button;

	int alarm;
	int count;
	
	while(count<5)
	{
		motion_detector = a; 
		window_sensor = b; 
		master_switch = c;
		deactivate_system = d;
		button = e;
		window_sensor = 1;
		button = 0;
		if(master_switch == 1)
		{
			if(motion_detector==1)
			{
				if(window_sensor==1)
				{
					alarm = 1;
					if(button == 1)
					{
						alarm = 0;
					}
				}
				else
				{
					alarm = 1;
					if(button == 1)
					{
						alarm = 1;
					}
				}
			}
			else
			{
				if(window_sensor==1)
				{
					alarm = 0;
				}
				else
				{
					alarm = 1;
					if(button == 1)
					{
						alarm = 0;
					}
				}
				
			}
		}
		else
		{
			alarm = 0;
		}
		count++;
	}	
}

void main() {
	int a,b,c,d,e;
	klee_make_symbolic(&a, sizeof(a), "a");
	klee_make_symbolic(&a, sizeof(b), "b");
	klee_make_symbolic(&a, sizeof(c), "c");
	klee_make_symbolic(&a, sizeof(d), "d");
	klee_make_symbolic(&a, sizeof(e), "e");
	pgm(a,b,c,d,e);
}