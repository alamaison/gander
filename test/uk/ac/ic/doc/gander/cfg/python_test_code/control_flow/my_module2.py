def test_basic():
	a
	b

def test_if():
	a
	if b:
		c

def test_if_else():
	a
	if b:
		c
	else:
		d

def test_if_fallthru():
	a
	if b:
		c
	d

def test_if_else_fallthru():
	a
	if b:
		c
	else:
		d
	e

def test_while():
	a
	while b:
		c
	d
	
def test_nested():
	a
	if b:
		c
		while d:
			e
		f
	else:
		g
	h

def test_nested_while_if():
	while a:
		b
		if c:
			d
		e

def test_nested_while_if_break():
	while a:
		b
		if c:
			d
			break
		e

def test_nested_while_if_break_else():
	while a:
		b
		if c:
			d
			break
		else:
			e
		f

def test_nested_whiles_break():
	while a:
		b
		while c:
			d
			break

def test_nested_whiles_if_break():
	while a:
		b
		while c:
			d
			if e:
				break

def test_nested_whiles_break_fall():
	while a:
		b
		while c:
			d
			break
	e

def test_nested_ifs_break():
	while a:
		if b:
			if c:
				break
			d
	e
	
def test_if_else_break():
	while a:
		if b:
			c
		else:
			break
	d

def test_twopronged_fallthrough_to_while():
	if a:
		b
	else:
		c
	while d:
		e

def test_while_if_continue1():
	while a:
		if b:
			continue
		c

def test_while_if_continue2():
	while a:
		if b:
			c
			continue
		d

def test_return():
	a
	b
	return

def test_return_val():
	a
	return b
	
def test_cond_return():
	a
	if b:
		return c
	d

def test_while_return():
	a
	while b:
		c
		return
	d
	
def test_while_cond_return():
	a
	while b:
		c
		if d:
			return
		e
	f

def test_multiple_return():
	if a:
		return b
	else:
		return c

def test_multiple_return2():
	if a:
		b
		return
	else:
		while c:
			d
			if e:
				return
	f
	
def test_for():
	a
	for x in b:
		c
	
def test_for_break():
	for x in a:
		b
		if c:
			break
	
def test_for_continue():
	for x in a:
		if b:
			continue
		c
	
def test_for_return():
	for x in a:
		if b:
			return
		c

def test_yield1():
	a
	while b:
		yield c
		d

def test_yield2():
	a
	while b:
		yield c

def test_pass():
	pass

def test_if_pass():
	if a:
		pass
	b

def test_if_else_pass():
	if a:
		pass
	else:
		b
	c

def test_if_else_pass_pass():
	if a:
		pass
	else:
		pass
	b

def test_while_pass():
	while a:
		pass

def test_raise1():
	raise

def test_raise2():
	raise a
	
def test_raise3():
	a
	if b:
		raise c
	d

def test_try_except1():
	try:
		raise a
	except Exception:
		b

def test_try_except2():
	try:
		a
		raise b
	except Exception:
		c

def test_try_except3():
	try:
		if a:
			raise b
	except Exception:
		c
		
def test_try_except4():
	try:
		if a:
			raise b
	except ImportError:
		c
	except Exception:
		d

def test_try_except_all():
	try:
		if a:
			raise b
	except Exception:
		c
	except:
		d

def test_try_except_raise():
	try:
		raise a
	except Exception:
		raise b
		
def test_try_except_all_raise():
	try:
		raise a
	except:
		raise b

def test_try_except_else1():
	try:
		raise a
	except Exception:
		b
	else:
		c

def test_try_except_else2():
	try:
		if a:
			raise b
	except Exception:
		c
	else:
		d

def test_try_except_else3():
	try:
		a
	except Exception:
		b
	else:
		c

def test_try_except_else_raise():
	try:
		if a:
			raise b
	except Exception:
		c
	else:
		raise d

def test_try_except_empty():
	try:
		pass
	except Exception:
		a
	b


def test_try_except_empty_raise():
	try:
		raise
	except Exception:
		a
	b

def test_try_except_empty_else():
	try:
		pass
	except Exception:
		a
	else:
		b
		
def test_try_finally1():
	try:
		if a:
			raise b
	finally:
		if c:
			d
		else:
			e

def test_try_finally2():
	try:
		if a:
			b
	finally:
		if c:
			d
		else:
			e

def test_try_finally3():
	try:
		if a:
			return b
	finally:
		if c:
			d
		else:
			e

def test_try_finally4():
	try:
		if a:
			return b
		else:
			raise c
	finally:
		if d:
			e
		else:
			f

def test_try_finally5():
	while a:
		try:
			if b:
				break
		finally:
			if c:
				d
			else:
				e

def test_try_finally6():
	try:
		if a:
			raise b
		else:
			raise c
	finally:
		if d:
			e
		else:
			f

def test_tricky_try_except():
	# Here the empty 'except' body must attribute it's control-flow effect
	# to the raises of the 'try' body
	
	while a:
		try:
			if b:
				raise c
		except:
			break
	d

def test_double_empty_body():
	a
	try:
		raise
	finally:
		return
	b

def test_print():
	print a

def test_multiprint():
	print a, b

def test_print_continuing_same_block():
	a
	print b, c
