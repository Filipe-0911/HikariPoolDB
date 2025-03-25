package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Set;

public class ContaService {
    private ConnectionFactory conn;

    public ContaService() {
        this.conn = new ConnectionFactory();
    }

    public Set<Conta> listarContasAbertas() {
        Connection conn = new ConnectionFactory().recuperarConexao();
        ContaDAO contaDAO = new ContaDAO(conn);
        return contaDAO.listaDeContas();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta)  {
        Connection connection = conn.recuperarConexao();
        new ContaDAO(connection).salvar(dadosDaConta);

    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        BigDecimal novoValor = conta.getSaldo().subtract(valor);

        alterar(conta, novoValor);
    }
    private void alterar(Conta conta, BigDecimal valor) {
        Connection connection = conn.recuperarConexao();
        new ContaDAO(connection).alterarConta(conta.getNumero(), valor);
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        Conta conta = new ContaDAO(conn.recuperarConexao()).contaEspecifica(numeroDaConta);
        alterar(conta, conta.getSaldo().add(valor));
    }

    public void encerrar(Integer numeroDaConta) {

        Conta conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        new ContaDAO(conn.recuperarConexao()).deletarConta(numeroDaConta);
    }

    private Conta buscarContaPorNumero(Integer numero) {
        return new ContaDAO(conn.recuperarConexao()).contaEspecifica(numero);
    }

    public void realizarTransferencia(Integer numeroContaOrigem, Integer numeroContaDestino, BigDecimal valor) {
        this.realizarSaque(numeroContaOrigem, valor);
        this.realizarDeposito(numeroContaDestino, valor);

    }
}
