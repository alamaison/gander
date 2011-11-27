# f is called via an alias that was put into a dict. We can't track this alias
# so we have to conservatively assume f is called from anywhere so its parameter
# must be type Top

def f(x):
	print x # what_am_i

f("Hello")

d = {"callme" : f}

g = d["callme"]

g([]) # inferred type should include this list
