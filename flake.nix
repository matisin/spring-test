{
  inputs = {
    nixpkgs.url = "github:cachix/devenv-nixpkgs/rolling";
    systems.url = "github:nix-systems/default";
    devenv.url = "github:cachix/devenv";
    devenv.inputs.nixpkgs.follows = "nixpkgs";
  };

  nixConfig = {
    extra-trusted-public-keys = "devenv.cachix.org-1:w1cLUi8dv3hnoSPGAuibQv+f9TZLr6cv/Hm9XgU50cw=";
    extra-substituters = "https://devenv.cachix.org";
  };

  outputs = {
    self,
    nixpkgs,
    devenv,
    systems,
    ...
  } @ inputs: let
    forEachSystem = nixpkgs.lib.genAttrs (import systems);
  in {
    packages = forEachSystem (system: {
      devenv-up = self.devShells.${system}.default.config.procfileScript;
    });

    devShells = forEachSystem (system: let
      pkgs = import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
        };
      };
    in {
      default = devenv.lib.mkShell {
        inherit inputs pkgs;
        modules = [
          {
            # https://devenv.sh/reference/options/
            packages = with pkgs; [
              hello
              jdk17
              maven
              kafkactl
              mongodb
              redis

              # Herramientas adicionales que podrían ser útiles
              docker-compose
              spring-boot-cli

              jdt-language-server
              gopls
            ];

            languages.go.enable = true;

            enterShell = ''
              export JAVA_HOME=${pkgs.jdk17}
              export MAVEN_OPTS="-Dmaven.repo.local=$PWD/.m2/repository"
              hello
            '';

            processes.hello.exec = "hello";
          }
        ];
      };
    });
  };
}
