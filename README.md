# spring-test
Me hubiera gustado terminar este test y probablemente lo haga pero este es lo que alcance a terminar.

Voy a explicar aca lo que queria hacer y lo que falto.

Es un sistema dockerizado donde esta el worker de spring que se requeria, iba a hacer dos apis en 
Go (amo go) pero me enfoque mas en el worker.

Dividi el codigo del worker en una arquitectura hexagonal donde defini primero las interfaces en una
capa llamada domain y cree test para funcionalidades del servicio en capa application. Todo lo que es
codigo tecnico o dependencias se separan en capa adapters en primarias o secundarias, primaria seria 
lo que controla, los inputs. por ejemmplo una llamada http o un evento. esta capa no la hice, todo
lo que fuera de spring va en esta capa.

los adaptadores secundarios son sistemas que mi codigo controla. Puede ser una base de datos, una api,
un servico de autenticacion, un servicio cloud como s3. 

hice tests de integracion de spring, codigo que busca medir adaptadores primarios pero sin usar mocks
con algunas excepciones pero no alcance a pasar los tests.
 
Deje tests e2e en go pero fallan, ya que aún no se termina el sistema completo. estos tests iban a medir
el sistema junto con las apis y medir que todo estuviera correcto.

Este repositorio tambien viene con un ambiente de nix y devenv, que permite crear ambientes de desarrollo
por proyecto. Esto no reemplaza docker para desplegar el sistema.

Quedó pendiente las actions de github actions para deployar, buildear y proteger la rama principal u
otros ambientes, consiguiendo un proceso agil al desarrollar, ya que uno puede "romper" con tranquilidad
porque los test protegen la aplicacion. 

Al final de todo me iba a preocupar de prometheus pero esto hubiera ido como adaptador secundario
en la estructura que defini con anterioridad.

Espero que les guste mi codigo, mi forma de ver un sistema y podamos continuar trabajando juntos,
esto fue un desafio muy interesante y lo agradezco mucho.
