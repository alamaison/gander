from sibling import A, B, C

def main():
	if True:
		x = A()
	else:
		x = B()
	x.a("tag1")
	x.b("tag2")
	x.c("tag3")