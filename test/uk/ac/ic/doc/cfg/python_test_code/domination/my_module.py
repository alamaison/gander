def dom():
	a()
	b()

def dom_if():
	a()
	if b():
		c()

def dom_if_else():
	a()
	if b():
		c()
	else:
		d()

def dom_if_fallthru():
	a()
	if b():
		c()
	d()

def dom_if_else_fallthru():
	a()
	if b():
		c()
	else:
		d()
	e()

def dom_while():
	a()
	while b():
		c()
	d()
	
def dom_nested():
	a()
	if b():
		c()
		while d():
			e()
		f()
	else:
		g()
	h()

def dom_nested_while_if():
	while a():
		b()
		if c():
			d()
		e()

def dom_nested_while_if_break():
	while a():
		b()
		if c():
			d()
			break
		e()

def dom_nested_while_if_break_else():
	while a():
		b()
		if c():
			d()
			break
		else:
			e()
		f()

def dom_nested_whiles_break():
	while a():
		b()
		while c():
			d()
			break

def dom_nested_whiles_break_fall():
	while a():
		b()
		while c():
			d()
			break
	e()
