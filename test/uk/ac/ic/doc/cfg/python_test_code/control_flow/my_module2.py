def test_basic():
	a()
	b()

def test_if():
	a()
	if b():
		c()

def test_if_else():
	a()
	if b():
		c()
	else:
		d()

def test_if_fallthru():
	a()
	if b():
		c()
	d()

def test_if_else_fallthru():
	a()
	if b():
		c()
	else:
		d()
	e()

def test_while():
	a()
	while b():
		c()
	d()
	
def test_nested():
	a()
	if b():
		c()
		while d():
			e()
		f()
	else:
		g()
	h()

def test_nested_while_if():
	while a():
		b()
		if c():
			d()
		e()

def test_nested_while_if_break():
	while a():
		b()
		if c():
			d()
			break
		e()

def test_nested_while_if_break_else():
	while a():
		b()
		if c():
			d()
			break
		else:
			e()
		f()

def test_nested_whiles_break():
	while a():
		b()
		while c():
			d()
			break

def test_nested_whiles_if_break():
	while a():
		b()
		while c():
			d()
			if e():
				break

def test_nested_whiles_break_fall():
	while a():
		b()
		while c():
			d()
			break
	e()

def test_nested_ifs_break():
	while a():
		if b():
			if c():
				break
			d()
	e()
	
def test_if_else_break():
	while a():
		if b():
			c()
		else:
			break
	d()

def test_twopronged_fallthrough_to_while():
	if a():
		b()
	else:
		c()
	while d():
		e()

def test_while_if_continue1():
	while a():
		if b():
			continue
		c()

def test_while_if_continue2():
	while a():
		if b():
			c()
			continue
		d()

def test_return():
	a()
	b()
	return

def test_return_val():
	a()
	return b()
	
def test_cond_return():
	a()
	if b():
		return c()
	d()

def test_while_return():
	a()
	while b():
		c()
		return
	d()
	
def test_while_cond_return():
	a()
	while b():
		c()
		if d():
			return
		e()
	f()

def test_multiple_return():
	if a():
		return b()
	else:
		return c()

def test_multiple_return2():
	if a():
		b()
		return
	else:
		while c():
			d()
			if e():
				return
	f()
	
def test_for():
	a()
	for x in b():
		c()
	
def test_for_break():
	for x in a():
		b()
		if c():
			break
	
def test_for_continue():
	for x in a():
		if b():
			continue
		c()
	
def test_for_return():
	for x in a():
		if b():
			return
		c()
