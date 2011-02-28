def fun():
	x = y
	x.x() # length 2
	x.y() # length 2
	if p:
		x.z() # length 3

def bun():
	while z.m(): # length 1
		z.a() # length 2

def duplicates():
	a = b
	a.a() # length 1
	a.a() # length 1
	a.a() # length 1
	a.a() # length 1
	
def args_ignored_for_the_moment():
	g = h
	g.a() # length 1
	g.a("some string") # length 1
	g.a(42) # length 1
	
def args_searched_for_other_calls():
	g = h
	g.e(g.f()) # length 2, length 2
	

import sys

def ignore():
	free_function()
	free_function()
	another_free_function()
	sys.imported_function()
	sys.another_imported_function()
