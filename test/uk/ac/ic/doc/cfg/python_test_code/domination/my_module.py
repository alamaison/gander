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
