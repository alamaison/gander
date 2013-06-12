class Base:
	def a(self):
		pass
	def b(self):
		pass

class A(Base):
	def c(self):
		pass

class B(Base):
	pass

class C(Base):
	def d(self):
		pass

def main():
	if True:
		x = A()
	else:
		x = B()
	x.a("tag1")
	x.b("tag2")
	x.c("tag3")