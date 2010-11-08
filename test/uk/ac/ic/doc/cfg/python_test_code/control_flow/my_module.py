def my_fun():
	x = 3
	y.m()

def my_fun_if():
	a()
	if b():
		c()

def my_fun_if_else():
	a()
	if b():
		c()
	else:
		d()

def my_fun_if_fallthru():
	a()
	if b():
		c()
	d()

def my_fun_if_else_fallthru():
	a()
	if b():
		c()
	else:
		d()
	e()

def my_fun_while():
	a()
	while b():
		c()
		
def my_fun_while_fallthru():
	a()
	while b():
		c()
	d()

def my_fun_nested_if():
	a()
	if b():
		if c():
			d()
	e()

