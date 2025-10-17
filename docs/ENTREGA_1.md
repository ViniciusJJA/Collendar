**Collendar(Por enquanto)) - Calendário Compartilhado**

**Centro Universitário SENAI Santa Catarina**
**Curso:** Análise e Desenvolvimento de Sistemas (4ª Fase)
**UC:** Desenvolvimento de Sistemas Web
**Docente:** Clávison M. Zapelini
**Dupla:** Henrique e Vinícius

**VISÃO GERAL DO PROJETO**

**Descrição da Funcionalidade**
Uma aplicação mobile de calendário compartilhado onde usuários podem criar eventos e compartilhar calendários com outras pessoas. 
Permitindo que grupos de pessoas (equipes, famílias, amigos) sincronizem eventos em um calendário central, como aniverários, viagens ou outros tipos de compromisso.

**ESCOPO MÍNIMO**

**Autenticação e Autorização:** Login/registro com JWT, roles (USER, ADMIN), autenticação em endpoints protegidos
**CRUD de Usuários:** Criar conta, atualizar perfil, deletar conta
**CRUD de Calendários:** Criar, editar, deletar calendários pessoais
**CRUD de Eventos:** Criar, editar, deletar eventos dentro de calendários
**Compartilhamento de Calendários:** Compartilhar calendário com outro usuário, definir permissões (VISUALIZAR, EDITAR), remover compartilhamento
**Paginação:** Calendários com listagem paginada
**Filtros:** Filtrar eventos por data inicial/final, título, calendário e criador

**Total de entidades/recursos:** 5 entidades principais (Usuario, Calendario, Evento, Compartilhamento, Role)

**ESCOPO OPCIONAL**

Categorias de eventos com cores personalizadas
Recorrência de eventos (semanal, mensal, anual)
Convites com aceitar/rejeitar
Dashboard com estatísticas (total eventos, calendários compartilhados)
Dark mode no frontend

**DIAGRAMA DE CLASSES UML**

classDiagram
        
    class Role {
        -id: Long
        -nome: String [USER, ADMIN] [UNIQUE]
    }

    class Usuario {
        -id: Long
        -email: String [UNIQUE]
        -nome: String
        -senha: String [BCrypt]
        -dataCriacao: LocalDateTime
        -roles: Set~Role~
        --
        +registrar(email, nome, senha)
        +autenticar(email, senha): String
        +atualizarPerfil(nome, senha)
        +deletarConta()
        +buscarPorEmail(email): Usuario
    }

    class Calendario {
        -id: Long
        -nome: String
        -descricao: String
        -cor: String
        -dataCriacao: LocalDateTime
        -usuarioProprietario: Usuario
        --
        +criar(usuario): Calendario
        +editar(nome, descricao, cor)
        +deletar()
        +compartilhar(usuario, permissao): Compartilhamento
        +validarAcesso(usuario): boolean
        +listarEventos(): List~Evento~
    }

    class Evento {
        -id: Long
        -titulo: String
        -descricao: String
        -dataInicio: LocalDateTime
        -dataFim: LocalDateTime
        -local: String
        -calendario: Calendario
        -criador: Usuario
        -dataCriacao: LocalDateTime
        --
        +criar(calendario, usuario): Evento
        +editar(titulo, descricao, datas)
        +deletar()
        +validarDatas(): boolean
        +verificarConflito(): boolean
    }

    class Compartilhamento {
        -id: Long
        -calendario: Calendario
        -usuarioCompartilhado: Usuario
        -permissao: String [VISUALIZAR, EDITAR]
        -dataCriacao: LocalDateTime
        --
        +concederAcesso(calendario, usuario, permissao)
        +revogarAcesso()
        +atualizarPermissao(permissao)
        +validarPermissao(usuario, acao): boolean
    }

    Usuario "1" --> "*" Calendario : proprietário
    Usuario "*" --> "*" Role : possui
    Calendario "1" --> "*" Evento : contém
    Usuario "1" --> "*" Evento : cria
    Calendario "1" --> "*" Compartilhamento : é compartilhado
    Usuario "1" --> "*" Compartilhamento : recebe acesso

**MODELAGEM ER SIMPLIFICADA**

erDiagram
USUARIO ||--o{ APP_USER_ROLES : "possui"
ROLE ||--o{ APP_USER_ROLES : "atribuída"
USUARIO ||--o{ CALENDARIO : "proprietário"
USUARIO ||--o{ EVENTO : "criador"
USUARIO ||--o{ COMPARTILHAMENTO : "recebe"
CALENDARIO ||--o{ EVENTO : "contém"
CALENDARIO ||--o{ COMPARTILHAMENTO : "compartilhado"

    ROLE {
        BIGINT id PK
        VARCHAR nome
    }

    USUARIO {
        BIGINT id PK
        VARCHAR email
        VARCHAR nome
        VARCHAR senha "BCrypt"
        TIMESTAMP data_criacao
    }

    APP_USER_ROLES {
        BIGINT usuario_id FK
        BIGINT role_id FK
    }

    CALENDARIO {
        BIGINT id PK
        VARCHAR nome
        TEXT descricao
        VARCHAR cor
        TIMESTAMP data_criacao
        BIGINT usuario_proprietario_id FK
    }

    EVENTO {
        BIGINT id PK
        VARCHAR titulo
        TEXT descricao
        TIMESTAMP data_inicio
        TIMESTAMP data_fim 
        VARCHAR local 
        BIGINT calendario_id FK
        BIGINT criador_id FK
        TIMESTAMP data_criacao
    }

    COMPARTILHAMENTO {
        BIGINT id PK 
        BIGINT calendario_id FK
        BIGINT usuario_compartilhado_id FK 
        VARCHAR permissao "VISUALIZAR, EDITAR"
        TIMESTAMP data_criacao
    }

**ESTRUTURA DE PASTAS PROPOSTA**

**Backend**

src/
├── main/
│   ├── java/br/com/senai/calendario/
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── CalendarioController.java
│   │   │   ├── EventoController.java
│   │   │   └── UsuarioController.java
│   │   │
│   │   ├── service/
│   │   │   ├── UsuarioService.java
│   │   │   ├── CalendarioService.java
│   │   │   ├── EventoService.java
│   │   │   └── CompartilhamentoService.java
│   │   │
│   │   ├── repository/
│   │   │   ├── UsuarioRepository.java
│   │   │   ├── CalendarioRepository.java
│   │   │   ├── EventoRepository.java
│   │   │   ├── CompartilhamentoRepository.java
│   │   │   └── RoleRepository.java
│   │   │
│   │   ├── model/
│   │   │   ├── Usuario.java
│   │   │   ├── Calendario.java
│   │   │   ├── Evento.java
│   │   │   ├── Compartilhamento.java
│   │   │   └── Role.java
│   │   │
│   │   ├── dto/
│   │   │   ├── UsuarioDTO.java
│   │   │   ├── CalendarioDTO.java
│   │   │   ├── EventoDTO.java
│   │   │   ├── CompartilhamentoDTO.java
│   │   │   ├── LoginDTO.java
│   │   │   ├── LoginResponseDTO.java
│   │   │   └── EventoFiltroDTO.java
│   │   │
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── CorsConfig.java
│   │   │
│   │   ├── security/
│   │   │   ├── JwtUtil.java
│   │   │   ├── JwtAuthFilter.java
│   │   │   └── UserDetailsServiceImpl.java
│   │   │
│   │   ├── specification/
│   │   │   └── EventoSpecification.java
│   │   │
│   │   └── CalendarioApplication.java
│   │
│   └── resources/
│       ├── application.properties
│       └── db/migration/
│           └── V1__create_tables.sql
│
└── test/
├── java/br/com/senai/calendario/
│   ├── controller/
│   │   ├── AuthControllerTest.java
│   │   ├── CalendarioControllerTest.java
│   │   └── EventoControllerTest.java
│   │
│   ├── service/
│   │   ├── UsuarioServiceTest.java
│   │   ├── CalendarioServiceTest.java
│   │   └── EventoServiceTest.java
│   │
│   └── repository/
│       ├── UsuarioRepositoryTest.java
│       └── CalendarioRepositoryTest.java
│
└── resources/
├── application-test.properties
└── data.sql

**Frontend**
collendar-app/
├── src/
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.js
│   │   │   └── RegisterScreen.js
│   │   │
│   │   ├── calendar/
│   │   │   ├── CalendarListScreen.js
│   │   │   ├── CalendarDetailScreen.js
│   │   │   └── CreateCalendarScreen.js
│   │   │
│   │   ├── event/
│   │   │   ├── EventListScreen.js
│   │   │   ├── EventDetailScreen.js
│   │   │   └── CreateEventScreen.js
│   │   │
│   │   └── profile/
│   │       └── ProfileScreen.js
│   │
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.js
│   │   │   ├── Input.js
│   │   │   └── Loading.js
│   │   │
│   │   ├── calendar/
│   │   │   ├── CalendarCard.js
│   │   │   └── CalendarItem.js
│   │   │
│   │   └── event/
│   │       ├── EventCard.js
│   │       └── EventItem.js
│   │
│   ├── services/
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── calendarService.js
│   │   └── eventService.js
│   │
│   ├── utils/
│   │   ├── dateUtils.js
│   │   ├── validation.js
│   │   └── storage.js
│   │
│   ├── context/
│   │   └── AuthContext.js
│   │
│   ├── navigation/
│   │   ├── AppNavigator.js
│   │   └── AuthNavigator.js
│   │
│   └── assets/
│       ├── images/
│       └── icons/
│
├── App.js
├── package.json
└── README.md