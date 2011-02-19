def func_with_single_parm(z):
	pass

def func_with_single_parm2(z):
	pass

def func_with_two_parms(p, q):
	pass


def pass_single_position():
	x = bob()
	func_with_single_parm(x)

def pass_single_position_twice_same_parm():
	x = bob()
	func_with_single_parm(x)
	func_with_single_parm(x)

def pass_single_position_twice_different_parm():
	x = bob()
	y = sally()
	func_with_single_parm(x)
	func_with_single_parm(y)

def pass_single_position_twice_different_func():
	x = bob()
	func_with_single_parm(x)
	func_with_single_parm2(x)

def pass_single_position_twice_different_parm_different_func():
	x = bob()
	y = sally()
	func_with_single_parm(x)
	func_with_single_parm2(y)

def ignore_passing_compound_expressions():
	x = bob()
	y = sally()
	func_with_single_parm(x + y)
	func_with_single_parm2(y)

def pass_two_position():
	x = bob()
	y = sally()
	func_with_two_parms(x, y)
	
def pass_same_var_twice_position():
	x = bob()
	func_with_two_parms(x, x)

def pass_single_keyword():
	x = bob()
	func_with_single_parm(z=x)

def pass_two_keywords_usual_order():
	x = bob()
	y = sally()
	func_with_two_parms(p=x, q=y)
	
def pass_two_keywords_out_of_order():
	x = bob()
	y = sally()
	func_with_two_parms(q=x, p=y)

def pass_same_var_twice_keyword():
	x = bob()
	func_with_two_parms(p=x, q=x)

def pass_same_var_twice_mixed():
	x = bob()
	func_with_two_parms(x, q=x)

