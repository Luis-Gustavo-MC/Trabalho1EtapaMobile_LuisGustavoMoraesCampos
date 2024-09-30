package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

data class Feriado(
    val nome: String,
    val data: Calendar,
    val tipo: TipoFeriado,
    val estado: String? = null,
    val cidade: String? = null
)

enum class TipoFeriado {
    NACIONAL,
    ESTADUAL,
    MUNICIPAL
}

class MainActivity : AppCompatActivity() {

    val listaFeriados = ArrayList<Feriado>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCadastrarFeriado: Button = findViewById(R.id.btnCadastrarFeriado)
        val btnEditarFeriado: Button = findViewById(R.id.btnEditarFeriado)
        val btnRemoverFeriado: Button = findViewById(R.id.btnRemoverFeriado)
        val tvDataSelecionada: TextView = findViewById(R.id.tvDataSelecionada)
        val tvFeriados: TextView = findViewById(R.id.tvFeriados)

        btnCadastrarFeriado.setOnClickListener {
            mostrarDatePickerDialog(tvDataSelecionada, tvFeriados)
        }

        btnEditarFeriado.setOnClickListener {
            editarFeriadoDialog(tvFeriados)
        }

        btnRemoverFeriado.setOnClickListener {
            removerFeriadoDialog(tvFeriados)
        }
    }

    private fun mostrarDatePickerDialog(tvDataSelecionada: TextView, tvFeriados: TextView) {
        val calendarioAtual = Calendar.getInstance()
        val ano = calendarioAtual.get(Calendar.YEAR)
        val mes = calendarioAtual.get(Calendar.MONTH)
        val dia = calendarioAtual.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                val dataSelecionada = Calendar.getInstance().apply {
                    set(anoSelecionado, mesSelecionado, diaSelecionado)
                }

                if (dataSelecionada.after(calendarioAtual)) {
                    tvDataSelecionada.text = "Data Selecionada: $diaSelecionado/${mesSelecionado + 1}/$anoSelecionado"
                    escolherTipoFeriado(dataSelecionada, tvFeriados)
                } else {
                    tvDataSelecionada.text = "Erro: Data passada!"
                }
            },
            ano, mes, dia
        )
        datePickerDialog.show()
    }

    private fun escolherTipoFeriado(data: Calendar, tvFeriados: TextView) {
        val tipos = arrayOf("Nacional", "Estadual", "Municipal")
        var tipoSelecionado = TipoFeriado.NACIONAL

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione o tipo de feriado")
        builder.setSingleChoiceItems(tipos, 0) { _, which ->
            tipoSelecionado = when (which) {
                0 -> TipoFeriado.NACIONAL
                1 -> TipoFeriado.ESTADUAL
                2 -> TipoFeriado.MUNICIPAL
                else -> TipoFeriado.NACIONAL
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            when (tipoSelecionado) {
                TipoFeriado.NACIONAL -> {
                    cadastrarFeriado("Feriado Nacional", data, TipoFeriado.NACIONAL, tvFeriados)
                }
                TipoFeriado.ESTADUAL -> {
                    solicitarEstado(data, TipoFeriado.ESTADUAL, tvFeriados)
                }
                TipoFeriado.MUNICIPAL -> {
                    solicitarEstadoCidade(data, TipoFeriado.MUNICIPAL, tvFeriados)
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun solicitarEstado(data: Calendar, tipo: TipoFeriado, tvFeriados: TextView) {
        val inputEstado = EditText(this)
        inputEstado.hint = "Digite o estado (Ex: MG)"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Informe o estado")
        builder.setView(inputEstado)
        builder.setPositiveButton("OK") { _, _ ->
            val estado = inputEstado.text.toString()
            if (estado.isNotBlank()) {
                cadastrarFeriado("Feriado Estadual", data, tipo, tvFeriados, estado)
            } else {
                Toast.makeText(this, "Estado não pode estar vazio!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun solicitarEstadoCidade(data: Calendar, tipo: TipoFeriado, tvFeriados: TextView) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val inputEstado = EditText(this)
        inputEstado.hint = "Digite o estado (Ex: SP)"
        layout.addView(inputEstado)

        val inputCidade = EditText(this)
        inputCidade.hint = "Digite a cidade"
        layout.addView(inputCidade)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Informe o estado e a cidade")
        builder.setView(layout)
        builder.setPositiveButton("OK") { _, _ ->
            val estado = inputEstado.text.toString()
            val cidade = inputCidade.text.toString()

            if (estado.isNotBlank() && cidade.isNotBlank()) {
                cadastrarFeriado("Feriado Municipal", data, tipo, tvFeriados, estado, cidade)
            } else {
                Toast.makeText(this, "Estado e cidade não podem estar vazios!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun cadastrarFeriado(
        nome: String,
        data: Calendar,
        tipo: TipoFeriado,
        tvFeriados: TextView,
        estado: String? = null,
        cidade: String? = null
    ) {
        val feriado = Feriado(nome, data, tipo, estado, cidade)
        listaFeriados.add(feriado)
        Toast.makeText(this, "Feriado cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
        exibirFeriados(tvFeriados)
    }

    private fun exibirFeriados(tvFeriados: TextView) {
        val sb = StringBuilder()
        for ((index, feriado) in listaFeriados.withIndex()) {
            sb.append("$index. Nome: ${feriado.nome}, Data: ${feriado.data.time}, Tipo: ${feriado.tipo}\n")
            feriado.estado?.let { sb.append("Estado: $it\n") }
            feriado.cidade?.let { sb.append("Cidade: $it\n") }
            sb.append("\n")
        }
        tvFeriados.text = sb.toString()
    }

    private fun removerFeriadoDialog(tvFeriados: TextView) {
        if (listaFeriados.isEmpty()) {
            Toast.makeText(this, "Nenhum feriado cadastrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val nomesFeriados = listaFeriados.map { it.nome }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione um feriado para remover")
        builder.setItems(nomesFeriados) { _, which ->
            listaFeriados.removeAt(which)
            Toast.makeText(this, "Feriado removido com sucesso!", Toast.LENGTH_SHORT).show()
            exibirFeriados(tvFeriados)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun editarFeriadoDialog(tvFeriados: TextView) {
        if (listaFeriados.isEmpty()) {
            Toast.makeText(this, "Nenhum feriado cadastrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val nomesFeriados = listaFeriados.map { it.nome }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione um feriado para editar")
        builder.setItems(nomesFeriados) { _, which ->
            val feriado = listaFeriados[which]
            editarFeriado(which, feriado, tvFeriados)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun editarFeriado(indice: Int, feriado: Feriado, tvFeriados: TextView) {
        val inputNome = EditText(this)
        inputNome.setText(feriado.nome)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Feriado")
        builder.setView(inputNome)
        builder.setPositiveButton("OK") { _, _ ->
            val novoNome = inputNome.text.toString()
            if (novoNome.isNotBlank()) {
                listaFeriados[indice] = feriado.copy(nome = novoNome)
                Toast.makeText(this, "Feriado editado com sucesso!", Toast.LENGTH_SHORT).show()
                exibirFeriados(tvFeriados)
            } else {
                Toast.makeText(this, "Nome não pode estar vazio!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}
